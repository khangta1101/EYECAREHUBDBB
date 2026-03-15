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
public class InventoryStockDTO {
    private Long locationId;
    private String locationName;
    private Long variantId;
    private String variantName;
    private String productNo;
    private Integer onHandQty;
    private Integer reservedQty;
    private Integer availableQty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
