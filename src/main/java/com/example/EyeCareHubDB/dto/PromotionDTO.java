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
public class PromotionDTO {
    private Long id;
    private String code;
    private String name;
    private String promoType;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscount;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String ruleJson;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
