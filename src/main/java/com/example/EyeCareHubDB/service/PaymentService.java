package com.example.EyeCareHubDB.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;
import com.example.EyeCareHubDB.entity.Payment;
import com.example.EyeCareHubDB.entity.Payment.PaymentStatus;
import com.example.EyeCareHubDB.repository.OrderRepository;
import com.example.EyeCareHubDB.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
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
}
