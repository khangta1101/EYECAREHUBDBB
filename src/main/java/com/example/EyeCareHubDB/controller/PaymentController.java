package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.Payment;
import com.example.EyeCareHubDB.entity.Payment.PaymentStatus;
import com.example.EyeCareHubDB.service.PaymentService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Tag(name = "Payment")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.createPayment(payment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Payment> updateStatus(@PathVariable Long id,
                                                 @RequestParam PaymentStatus status,
                                                 @RequestParam(required = false) String transactionRef) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status, transactionRef));
    }
}
