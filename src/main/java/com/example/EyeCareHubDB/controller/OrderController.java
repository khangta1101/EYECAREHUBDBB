package com.example.EyeCareHubDB.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.EyeCareHubDB.dto.CheckoutRequest;
import com.example.EyeCareHubDB.dto.OrderDTO;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;
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
    public ResponseEntity<OrderDTO> checkout(@RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderDTO>> getByCustomer(
            @PathVariable("customerId") Long customerId,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId, status, PageRequest.of(page, size)));
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAll(
            @RequestParam(value = "status", required = false) OrderStatus status,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(status, pageable));
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatuses() {
        return ResponseEntity.ok(orderService.getOrderStatuses());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateStatus(@PathVariable("id") Long id,
                                               @RequestParam("status") OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}
