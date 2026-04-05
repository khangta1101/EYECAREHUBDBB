package com.example.EyeCareHubDB.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.EyeCareHubDB.dto.OrderStatusUpdateResponse;
import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;
import com.example.EyeCareHubDB.entity.Order.OrderType;
import com.example.EyeCareHubDB.service.OrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Order")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestParam("customerId") Long customerId,
                                           @RequestParam(value = "addressId", required = false) Long addressId,
                                           @RequestParam("orderType") OrderType orderType,
                                           @RequestParam(value = "promotionCode", required = false) String promotionCode,
                                           @RequestParam(value = "shippingFee", defaultValue = "0") BigDecimal shippingFee) {
        return ResponseEntity.ok(orderService.checkout(customerId, addressId, orderType, promotionCode, shippingFee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<Order>> getByCustomer(
            @PathVariable("customerId") Long customerId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId, PageRequest.of(page, size)));
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getAll(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(orderService.getAllOrders(PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderStatusUpdateResponse> updateStatus(@PathVariable("id") Long id,
                                                                  @RequestParam("status") OrderStatus status) {
        Order updated = orderService.updateStatus(id, status);
        return ResponseEntity.ok(OrderStatusUpdateResponse.builder()
            .orderId(updated.getId())
            .orderNo(updated.getOrderNo())
            .status(updated.getStatus())
            .build());
    }
}
