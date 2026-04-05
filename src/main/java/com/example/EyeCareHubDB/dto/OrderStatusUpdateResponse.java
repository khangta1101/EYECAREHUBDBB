package com.example.EyeCareHubDB.dto;

import com.example.EyeCareHubDB.entity.Order.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateResponse {
    private Long orderId;
    private String orderNo;
    private OrderStatus status;
}
