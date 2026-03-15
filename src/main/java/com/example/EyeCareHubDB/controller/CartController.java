package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.dto.CartDTO;
import com.example.EyeCareHubDB.dto.CartItemDTO;
import com.example.EyeCareHubDB.entity.CartItem;
import com.example.EyeCareHubDB.service.CartService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Cart")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{customerId}")
    public ResponseEntity<CartDTO> getCart(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(cartService.getCartDTO(customerId));
    }

    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartItemDTO> addItem(@PathVariable("customerId") Long customerId,
                                             @RequestParam("variantId") Long variantId,
                                             @RequestParam("qty") int qty,
                                             @RequestParam(value = "prescriptionId", required = false) Long prescriptionId,
                                             @RequestParam(value = "isPreorder", required = false) Boolean isPreorder,
                                             @RequestParam(value = "expectedAt", required = false) LocalDateTime expectedAt) {
        return ResponseEntity.ok(cartService.toDTO(cartService.addItem(customerId, variantId, qty, prescriptionId, isPreorder, expectedAt)));
    }

    @GetMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemDTO> getCartItem(@PathVariable("cartItemId") Long cartItemId) {
        return ResponseEntity.ok(cartService.getCartItemDTO(cartItemId));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateItem(@PathVariable("cartItemId") Long cartItemId,
                                                @RequestParam("qty") int qty) {
        CartItem updated = cartService.updateItem(cartItemId, qty);
        if (updated == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(cartService.toDTO(updated));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable("cartItemId") Long cartItemId) {
        cartService.removeItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{customerId}/items")
    public ResponseEntity<List<CartItemDTO>> getItems(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(cartService.getCartItems(customerId));
    }
}
