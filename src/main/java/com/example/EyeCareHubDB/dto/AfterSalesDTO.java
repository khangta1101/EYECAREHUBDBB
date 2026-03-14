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
public class AfterSalesDTO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String type;
    private String status;
    private String reason;
    private String itemsJson;
    private String evidenceUrls;
    private Long requestedById;
    private String requestedByName;
    private Long handledById;
    private String handledByName;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
