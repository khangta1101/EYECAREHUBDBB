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

import com.example.EyeCareHubDB.dto.CreatePaymentRequest;
import com.example.EyeCareHubDB.dto.PaymentDTO;
import com.example.EyeCareHubDB.dto.VnPayCallbackResponse;
import com.example.EyeCareHubDB.dto.VnPayCreatePaymentRequest;
import com.example.EyeCareHubDB.dto.VnPayCreatePaymentResponse;
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

    @PostMapping("/confirm")
    public PaymentDTO confirm(@RequestBody Map<String, String> body) {
        String txnRef = body.get("transactionRef");
        return paymentService.confirmPayment(txnRef);
    }

    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody CreatePaymentRequest payment) {
        return ResponseEntity.ok(paymentService.createPayment(payment));
    }

    @PostMapping("/vnpay/create")
    public ResponseEntity<VnPayCreatePaymentResponse> createVnPayPayment(@RequestBody VnPayCreatePaymentRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(paymentService.createVnPayPayment(request, resolveClientIp(httpRequest)));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "VNPay Callback", description = "Endpoint for VNPay to return transaction result. Not intended for manual use.")
    @GetMapping({ "/vnpay/callback", "/vnpay-callback" })
    public ResponseEntity<VnPayCallbackResponse> vnpayCallback(@RequestParam Map<String, String> params) {

        VnPayCallbackResponse response = paymentService.handleVnPayCallback(params);

        return ResponseEntity.ok(response); // ✅ trả JSON
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentDTO>> getByOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable("id") Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentDTO> updateStatus(@PathVariable("id") Long id,
            @RequestParam("status") PaymentStatus status,
            @RequestParam(value = "transactionRef", required = false) String transactionRef) {
        return ResponseEntity.ok(paymentService.updateStatus(id, status, transactionRef));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();

        // Convert IPv6 loopback to IPv4 for VNPAY compatibility
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
