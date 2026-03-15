package com.example.EyeCareHubDB.dto;

import com.example.EyeCareHubDB.entity.Payment.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VnPayCallbackResponse {
    private Long paymentId;
    private String transactionRef;
    private PaymentStatus status;
    private boolean validSignature;
    private String responseCode;
    private String transactionStatus;
    private String message;
}
