package com.example.EyeCareHubDB.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Payment;
import com.example.EyeCareHubDB.entity.Payment.PaymentPurpose;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder(Order order);
    List<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionRef(String transactionRef);
    List<Payment> findByOrderAndPaymentPurpose(Order order, PaymentPurpose purpose);
}
