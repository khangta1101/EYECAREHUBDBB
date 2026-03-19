package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.EyeCareHubDB.dto.CartDTO;
import com.example.EyeCareHubDB.dto.CartItemDTO;
import com.example.EyeCareHubDB.dto.AddToCartRequest;
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
                                             @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItem(customerId, request));
    }

    @GetMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemDTO> getCartItem(@PathVariable("cartItemId") Long cartItemId) {
        return ResponseEntity.ok(cartService.getCartItemDTO(cartItemId));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateItem(@PathVariable("cartItemId") Long cartItemId,
                                                @RequestParam("qty") int qty) {
        CartItemDTO updated = cartService.updateItem(cartItemId, qty);
        if (updated == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(updated);
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
