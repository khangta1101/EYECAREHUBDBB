package com.example.EyeCareHubDB.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
    private Map<String, Long> orderStatusCounts;
    private Map<String, BigDecimal> revenueByStatus;
    private List<TopProductDTO> topProducts;
}
