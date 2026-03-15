package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VnPayService vnPayService;

    @Transactional
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Transactional
    public VnPayCreatePaymentResponse createVnPayPayment(VnPayCreatePaymentRequest request, String clientIp) {
        if (request == null || request.getOrderId() == null) {
            throw new RuntimeException("orderId is required");
        }

        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : order.getGrandTotal();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }

        Payment payment = Payment.builder()
            .order(order)
            .paymentPurpose(request.getPaymentPurpose() != null ? request.getPaymentPurpose() : PaymentPurpose.FINAL)
            .provider(PaymentProvider.VNPAY)
            .amount(amount)
            .status(PaymentStatus.PENDING)
            .build();

        payment = paymentRepository.save(payment);
        payment.setTransactionRef(buildTransactionRef(payment.getId()));
        payment = paymentRepository.save(payment);

        String paymentUrl = vnPayService.buildPaymentUrl(payment, clientIp, request.getOrderInfo(), request.getReturnUrl());
        return VnPayCreatePaymentResponse.builder()
            .paymentId(payment.getId())
            .transactionRef(payment.getTransactionRef())
            .status(payment.getStatus())
            .paymentUrl(paymentUrl)
            .build();
    }

    @Transactional
    public VnPayCallbackResponse handleVnPayCallback(Map<String, String> queryParams) {
        String txnRef = queryParams.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            throw new RuntimeException("Missing vnp_TxnRef");
        }

        Payment payment = paymentRepository.findByTransactionRef(txnRef)
            .orElseThrow(() -> new RuntimeException("Payment not found with transactionRef: " + txnRef));

        boolean validSignature = vnPayService.validateSignature(queryParams);
        if (!validSignature) {
            throw new RuntimeException("Invalid VNPay signature");
        }

        String responseCode = queryParams.getOrDefault("vnp_ResponseCode", "");
        String transactionStatus = queryParams.getOrDefault("vnp_TransactionStatus", "");
        boolean success = "00".equals(responseCode)
            && (transactionStatus.isBlank() || "00".equals(transactionStatus));

        PaymentStatus targetStatus = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        // Keep success status idempotent and avoid downgrading by duplicate callbacks.
        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != targetStatus) {
            payment = updatePaymentStatus(payment.getId(), targetStatus, txnRef);
        }
        payment.setRawResponseJson(vnPayService.serializeResponse(queryParams));
        payment = paymentRepository.save(payment);

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

    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus, String transactionRef) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        payment.setStatus(newStatus);
        if (transactionRef != null) payment.setTransactionRef(transactionRef);
        if (newStatus == PaymentStatus.SUCCESS) {
            payment.setPaidAt(LocalDateTime.now());
            // Auto-update order to CONFIRMED if FINAL or DEPOSIT payment succeeds
            Order order = payment.getOrder();
            if (order.getStatus() == OrderStatus.NEW) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }
        }
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    private String buildTransactionRef(Long paymentId) {
        return "VNP" + paymentId + System.currentTimeMillis();
    }
}
