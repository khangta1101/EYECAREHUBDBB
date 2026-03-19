package com.example.EyeCareHubDB.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private String paymentPurpose;
    private String provider;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String transactionRef;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
