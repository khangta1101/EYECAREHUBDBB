package com.example.EyeCareHubDB.dto;

import com.example.EyeCareHubDB.entity.Order.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {
    private Long customerId;
    private Long addressId;
    private OrderType orderType;
    private String promotionCode;
    private String note;
}
