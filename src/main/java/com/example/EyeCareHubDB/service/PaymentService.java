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

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VnPayService vnPayService;

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

    @Transactional
    public VnPayCallbackResponse handleVnPayCallback(Map<String, String> queryParams) {

        // ✅ LẤY TỪ VNPAY (QUAN TRỌNG NHẤT)
        String txnRef = queryParams.get("vnp_TxnRef");

        if (txnRef == null || txnRef.isBlank()) {
            return VnPayCallbackResponse.builder()
                    .status("FAILED")
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
                    .status("FAILED")
                    .message("Payment not found")
                    .build();
        }

        // ✅ check chữ ký
        boolean validSignature = vnPayService.validateSignature(queryParams);
        if (!validSignature) {
            return VnPayCallbackResponse.builder()
                    .paymentId(payment.getId())
                    .transactionRef(payment.getTransactionRef())
                    .status("FAILED")
                    .validSignature(false)
                    .message("Invalid signature")
                    .build();
        }

        String responseCode = queryParams.getOrDefault("vnp_ResponseCode", "");
        String transactionStatus = queryParams.getOrDefault("vnp_TransactionStatus", "");

        boolean success = "00".equals(responseCode)
                && (transactionStatus.isBlank() || "00".equals(transactionStatus));

        PaymentStatus targetStatus = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != targetStatus) {
            updateStatus(payment.getId(), targetStatus, txnRef);
        }

        // ✅ update order luôn (CÁI BẠN MUỐN)
        if (success) {
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CONFIRMED);
        }

        payment.setRawResponseJson(vnPayService.serializeResponse(queryParams));
        paymentRepository.save(payment);

        return VnPayCallbackResponse.builder()
                .paymentId(payment.getId())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus().name())
                .validSignature(true)
                .responseCode(responseCode)
                .transactionStatus(transactionStatus)
                .message(success ? "Payment successful" : "Payment failed")
                .build();
    }

    @Transactional
    public PaymentDTO updateStatus(Long paymentId, PaymentStatus newStatus, String transactionRef) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        payment.setStatus(newStatus);
        if (transactionRef != null)
            payment.setTransactionRef(transactionRef);
        if (newStatus == PaymentStatus.SUCCESS) {
            payment.setPaidAt(LocalDateTime.now());
            Order order = payment.getOrder();
            if (order.getStatus() == OrderStatus.NEW) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
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

    private String buildTransactionRef(Long paymentId) {
        return "VNP" + paymentId + System.currentTimeMillis();
    }

    @Transactional
    public PaymentDTO confirmPayment(String txnRef) {

        Payment payment = paymentRepository.findByTransactionRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());

            Order order = payment.getOrder();
            if (order.getStatus() == OrderStatus.NEW) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }

            paymentRepository.save(payment);
        }

        return toDTO(payment);
    }
}
