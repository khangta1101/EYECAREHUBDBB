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
public class ProductVariantDTO {
    @com.fasterxml.jackson.annotation.JsonProperty("variantId")
    @com.fasterxml.jackson.annotation.JsonAlias("id")
    private Long variantId;

    @com.fasterxml.jackson.annotation.JsonProperty("id")
    public Long getId() {
        return variantId;
    }

    private Long productId;
    private String sku;
    private String variantName;
    private String color;
    private String size;
    private String material;
    private String attributesJson;
    private String currency;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

