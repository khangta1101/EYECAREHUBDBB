package com.example.EyeCareHubDB.dto;

import java.util.List;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    @com.fasterxml.jackson.annotation.JsonProperty("productId")
    @com.fasterxml.jackson.annotation.JsonAlias("id")
    private Long productId;

    @com.fasterxml.jackson.annotation.JsonProperty("id")
    public Long getId() {
        return productId;
    }


    private String name;
    private String sku;
    private String searchTags;
    private String productType;
    private Long primaryCategoryId;
    private String brand;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<ProductMediaDTO> media;
    private List<ProductVariantDTO> variants;
}

