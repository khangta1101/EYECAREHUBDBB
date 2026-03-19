package com.example.EyeCareHubDB.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopProductDTO {
    private Long productId;
    private String productName;
    private Long totalSold;
    private BigDecimal totalRevenue;
}
