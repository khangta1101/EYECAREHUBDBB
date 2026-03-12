package com.example.EyeCareHubDB.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMediaDTO {
    private Long id;
    private Long productId;
    private Long variantId;
    private String type;
    private String url;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}

