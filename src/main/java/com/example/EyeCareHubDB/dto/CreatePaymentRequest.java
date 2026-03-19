package com.example.EyeCareHubDB.dto;

import java.math.BigDecimal;
import com.example.EyeCareHubDB.entity.Payment.PaymentPurpose;
import com.example.EyeCareHubDB.entity.Payment.PaymentProvider;
import com.example.EyeCareHubDB.entity.Payment.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    private Long orderId;
    private PaymentPurpose paymentPurpose;
    private PaymentProvider provider;
    private BigDecimal amount;
    @Builder.Default
    private String currency = "VND";
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    private String transactionRef;
}
