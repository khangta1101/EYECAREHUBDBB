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
public class CartItemDTO {
    private Long id;
    private Long cartId;
    private Long variantId;
    private String variantName;
    private String sku;
    private String imageUrl;
    private Integer qty;
    private BigDecimal unitPrice;
    private Boolean isPreorder;
    private LocalDateTime preorderExpectedAt;
    private Long prescriptionId;
    private LocalDateTime addedAt;
}
