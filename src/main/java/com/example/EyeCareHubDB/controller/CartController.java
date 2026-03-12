package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.Cart;
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
    public ResponseEntity<Cart> getCart(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartItem> addItem(@PathVariable Long customerId,
                                             @RequestParam Long variantId,
                                             @RequestParam int qty) {
        return ResponseEntity.ok(cartService.addItem(customerId, variantId, qty));
    }

    @PutMapping("/items/{cartId}/{variantId}")
    public ResponseEntity<CartItem> updateItem(@PathVariable Long cartId,
                                                @PathVariable Long variantId,
                                                @RequestParam int qty) {
        CartItem updated = cartService.updateItem(cartId, variantId, qty);
        if (updated == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/items/{cartId}/{variantId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartId,
                                            @PathVariable Long variantId) {
        cartService.removeItem(cartId, variantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{customerId}/items")
    public ResponseEntity<List<CartItem>> getItems(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.getCartItems(customerId));
    }
}
