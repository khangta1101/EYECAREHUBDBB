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
public class AddToCartRequest {
    private Long variantId;
    private int qty;
    private Long prescriptionId;
    private Boolean isPreorder;
    private LocalDateTime expectedAt;
}
