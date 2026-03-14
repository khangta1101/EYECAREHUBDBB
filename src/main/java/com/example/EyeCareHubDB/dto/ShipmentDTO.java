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
public class ShipmentDTO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String carrier;
    private String trackingNumber;
    private String trackingUrl;
    private String status;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime shippedAt;
    private LocalDateTime actualDelivery;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
