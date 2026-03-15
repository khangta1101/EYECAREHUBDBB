package com.example.EyeCareHubDB.dto;

import java.math.BigDecimal;

import com.example.EyeCareHubDB.entity.Payment.PaymentPurpose;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VnPayCreatePaymentRequest {
    private Long orderId;
    private PaymentPurpose paymentPurpose;
    private BigDecimal amount;
    private String orderInfo;
    private String returnUrl;
}
