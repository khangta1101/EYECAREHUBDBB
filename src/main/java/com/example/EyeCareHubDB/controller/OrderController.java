package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;
import com.example.EyeCareHubDB.entity.Order.OrderType;
import com.example.EyeCareHubDB.service.OrderService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Order")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestParam Long customerId,
                                           @RequestParam(required = false) Long addressId,
                                           @RequestParam OrderType orderType,
                                           @RequestParam(required = false) String promotionCode,
                                           @RequestParam(defaultValue = "0") BigDecimal shippingFee) {
        return ResponseEntity.ok(orderService.checkout(customerId, addressId, orderType, promotionCode, shippingFee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                               @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}
