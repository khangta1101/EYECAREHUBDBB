package com.example.EyeCareHubDB.dto;

import com.example.EyeCareHubDB.entity.Order.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDateTime;

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
    private List<CheckoutItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutItemRequest {
        private Long cartItemId;
        private PrescriptionDTO prescription;
        private LocalDateTime preorderExpectedAt;
        private String itemNote;
    }
}
