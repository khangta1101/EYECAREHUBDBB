package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.CreatePaymentRequest;
import com.example.EyeCareHubDB.dto.PaymentDTO;
import com.example.EyeCareHubDB.dto.VnPayCallbackResponse;
import com.example.EyeCareHubDB.dto.VnPayCreatePaymentRequest;
import com.example.EyeCareHubDB.dto.VnPayCreatePaymentResponse;
import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;
import com.example.EyeCareHubDB.entity.Payment;
import com.example.EyeCareHubDB.entity.Payment.PaymentProvider;
import com.example.EyeCareHubDB.entity.Payment.PaymentPurpose;
import com.example.EyeCareHubDB.entity.Payment.PaymentStatus;
import com.example.EyeCareHubDB.repository.OrderRepository;
import com.example.EyeCareHubDB.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;

// ============================================================
// SERVICE: PaymentService — Quản lý toàn bộ quy trình thanh toán.
// Hỗ trợ: VNPAY (online), COD, BANK_TRANSFER.
// PaymentStatus: PENDING → PAID / FAILED → REFUNDED
// Luồng VNPay: createVnPayPayment() → redirect → handleVnPayCallback() → updateStatus()
// ============================================================
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VnPayService vnPayService;
    private final FulfillmentService fulfillmentService;

    // Tạo payment thủ công (COD, BANK_TRANSFER). Amount ưu tiên request.amount → grandTotal.
    @Transactional
    public PaymentDTO createPayment(CreatePaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : order.getGrandTotal();

        Payment payment = Payment.builder()
                .order(order)
                .paymentPurpose(
                        request.getPaymentPurpose() != null ? request.getPaymentPurpose() : PaymentPurpose.FINAL)
                .provider(request.getProvider())
                .amount(amount)
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .status(request.getStatus() != null ? request.getStatus() : PaymentStatus.PENDING)
                .transactionRef(request.getTransactionRef())
                .build();

        return toDTO(paymentRepository.save(payment));
    }

    // Tạo link thanh toán VNPay. TransactionRef = "VNP" + paymentId + timestamp (unique).
    // Trả về paymentUrl để frontend redirect khách hàng sang trang VNPay.
    @Transactional
    public VnPayCreatePaymentResponse createVnPayPayment(VnPayCreatePaymentRequest request, String clientIp) {
        if (request == null || request.getOrderId() == null) {
            throw new RuntimeException("orderId is required");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));

        // Luôn sử dụng tổng tiền từ Order (đã bao gồm phí ship 30k)
        BigDecimal amount = order.getGrandTotal();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentPurpose(
                        request.getPaymentPurpose() != null ? request.getPaymentPurpose() : PaymentPurpose.FINAL)
                .provider(PaymentProvider.VNPAY)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        payment.setTransactionRef(buildTransactionRef(payment.getId()));
        payment = paymentRepository.save(payment);

        String paymentUrl = vnPayService.buildPaymentUrl(payment, clientIp, request.getOrderInfo(),
                request.getReturnUrl());
        return VnPayCreatePaymentResponse.builder()
                .paymentId(payment.getId())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus())
                .paymentUrl(paymentUrl)
                .build();
    }

    // ⭐ Xử lý callback từ VNPay sau khi khách thanh toán.
    // Bước 1: Lấy vnp_TxnRef → tìm Payment
    // Bước 2: Xác minh chữ ký HMAC-SHA512 → chống giả mạo
    // Bước 3: vnp_ResponseCode=00 → PAID, ngược lại → FAILED
    // Bước 4: Nếu PAID và Order còn NEW → CONFIRMED + tạo FulfillmentTask tự động
    // Bước 5: Lưu rawResponseJson để đối soát
    @Transactional
    public VnPayCallbackResponse handleVnPayCallback(Map<String, String> queryParams) {
        System.out.println("====== VNPAY CALLBACK DEBUG ======");
        queryParams.forEach((key, value) -> System.out.println("  " + key + " = " + value));

        // ✅ LẤY TỪ VNPAY (QUAN TRỌNG NHẤT)
        String txnRef = queryParams.get("vnp_TxnRef");

        if (txnRef == null || txnRef.isBlank()) {
            return VnPayCallbackResponse.builder()
                    .status(PaymentStatus.FAILED)
                    .message("Missing txnRef")
                    .build();
        }

        System.out.println("✅ TxnRef từ VNPAY: " + txnRef);

        Payment payment = paymentRepository
                .findByTransactionRef(txnRef)
                .orElse(null);

        if (payment == null) {
            System.out.println("❌ Không tìm thấy payment với txnRef: " + txnRef);
            return VnPayCallbackResponse.builder()
                    .status(PaymentStatus.FAILED)
                    .message("Payment not found")
                    .build();
        }

        // ✅ check chữ ký
        boolean validSignature = vnPayService.validateSignature(queryParams);
        if (!validSignature) {
            return VnPayCallbackResponse.builder()
                    .paymentId(payment.getId())
                    .transactionRef(payment.getTransactionRef())
                    .status(PaymentStatus.FAILED)
                    .validSignature(false)
                    .message("Invalid signature")
                    .build();
        }

        String responseCode = queryParams.getOrDefault("vnp_ResponseCode", "");
        String transactionStatus = queryParams.getOrDefault("vnp_TransactionStatus", "");

        boolean success = "00".equals(responseCode)
                && (transactionStatus.isBlank() || "00".equals(transactionStatus));

        PaymentStatus targetStatus = success ? PaymentStatus.PAID : PaymentStatus.FAILED;

        if (payment.getStatus() != PaymentStatus.PAID && payment.getStatus() != targetStatus) {
            updateStatus(payment.getId(), targetStatus, txnRef);
            // Refresh payment object to get latest status for response
            payment = paymentRepository.findById(payment.getId()).orElse(payment);
        }

        // ✅ Chỉ tự động CONFIRMED với đơn IN_STOCK và PRESCRIPTION.
        // Đơn PREORDER giữ nguyên NEW — Sale sẽ xác nhận thủ công → AWAITING.
        if (success) {
            Order order = payment.getOrder();
            if (order != null
                    && order.getStatus() == OrderStatus.NEW
                    && order.getOrderType() != Order.OrderType.PREORDER) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                fulfillmentService.generateTasksForOrder(order.getId());
                System.out.println("✅ Đã cập nhật trạng thái Order sang CONFIRMED và tạo tasks cho txnRef: " + txnRef);
            } else if (order != null && order.getOrderType() == Order.OrderType.PREORDER) {
                System.out.println("ℹ️ Đơn PREORDER #" + order.getId() + " giữ nguyên NEW — chờ Sale xác nhận.");
            }
        }

        payment.setRawResponseJson(vnPayService.serializeResponse(queryParams));
        paymentRepository.save(payment);

        return VnPayCallbackResponse.builder()
                .paymentId(payment.getId())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus())
                .validSignature(true)
                .responseCode(responseCode)
                .transactionStatus(transactionStatus)
                .message(success ? "Payment successful" : "Payment failed")
                .build();
    }

    // Cập nhật trạng thái Payment. Nếu PAID → ghi paidAt.
    // Chỉ tự động CONFIRMED cho đơn IN_STOCK/PRESCRIPTION. Đơn PREORDER giữ nguyên NEW.
    @Transactional
    public PaymentDTO updateStatus(Long paymentId, PaymentStatus newStatus, String transactionRef) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        payment.setStatus(newStatus);
        if (transactionRef != null)
            payment.setTransactionRef(transactionRef);
        if (newStatus == PaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
            Order order = payment.getOrder();
            // PREORDER: Sale xác nhận thủ công → giữ nguyên NEW
            if (order.getStatus() == OrderStatus.NEW
                    && order.getOrderType() != Order.OrderType.PREORDER) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                fulfillmentService.generateTasksForOrder(order.getId());
            }
        }
        return toDTO(paymentRepository.save(payment));
    }

    public List<PaymentDTO> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO getPayment(Long id) {
        return paymentRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    private PaymentDTO toDTO(Payment payment) {
        if (payment == null)
            return null;
        return PaymentDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentPurpose(payment.getPaymentPurpose().name())
                .provider(payment.getProvider().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .transactionRef(payment.getTransactionRef())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    // Tạo transactionRef duy nhất: "VNP" + paymentId + currentTimeMillis
    private String buildTransactionRef(Long paymentId) {
        return "VNP" + paymentId + System.currentTimeMillis();
    }

    // Xác nhận thanh toán thủ công theo txnRef (admin confirm). Chỉ chạy nếu chưa PAID.
    // PREORDER: giữ nguyên NEW, không tự động CONFIRMED.
    @Transactional
    public PaymentDTO confirmPayment(String txnRef) {

        Payment payment = paymentRepository.findByTransactionRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());

            Order order = payment.getOrder();
            // PREORDER: Sale xác nhận thủ công → không tự CONFIRMED ở đây
            if (order.getStatus() == OrderStatus.NEW
                    && order.getOrderType() != Order.OrderType.PREORDER) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }

            paymentRepository.save(payment);
        }

        return toDTO(payment);
    }
}
