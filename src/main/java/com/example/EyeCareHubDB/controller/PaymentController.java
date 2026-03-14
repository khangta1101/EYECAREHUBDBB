package com.example.EyeCareHubDB.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.EyeCareHubDB.dto.VnPayCallbackResponse;
import com.example.EyeCareHubDB.dto.VnPayCreatePaymentRequest;
import com.example.EyeCareHubDB.dto.VnPayCreatePaymentResponse;
import com.example.EyeCareHubDB.entity.Payment;
import com.example.EyeCareHubDB.entity.Payment.PaymentStatus;
import com.example.EyeCareHubDB.service.PaymentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

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

    @PostMapping("/vnpay/create")
    public ResponseEntity<VnPayCreatePaymentResponse> createVnPayPayment(@RequestBody VnPayCreatePaymentRequest request,
                                                                          HttpServletRequest httpRequest) {
        return ResponseEntity.ok(paymentService.createVnPayPayment(request, resolveClientIp(httpRequest)));
    }

    @GetMapping("/vnpay/callback")
    public ResponseEntity<VnPayCallbackResponse> handleVnPayCallback(@RequestParam Map<String, String> queryParams) {
        return ResponseEntity.ok(paymentService.handleVnPayCallback(queryParams));
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

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
