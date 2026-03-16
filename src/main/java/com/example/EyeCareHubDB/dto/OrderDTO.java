package com.example.EyeCareHubDB.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private String orderNo;
    private CustomerDTO customer;
    private AddressDTO shippingAddress;
    private AccountDTO salesStaff;
    private String channel;
    private String orderType;
    private String status;
    private PromotionDTO promotion;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal shippingFee;
    private BigDecimal grandTotal;
    private String note;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
}
