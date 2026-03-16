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
public class OrderItemDTO {
    private Long id;
    // user schema shows "order": "string", we'll omit it to avoid recursion or just use ID
    private ProductVariantDTO variant;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private Boolean isPrescription;
    private LocalDateTime preorderExpectedAt;
    private LocalDateTime preorderReceivedAt;
    private String itemNote;
    private PrescriptionDTO prescription;
}
