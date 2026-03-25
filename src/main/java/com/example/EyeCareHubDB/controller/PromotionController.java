package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.EyeCareHubDB.entity.Promotion;
import com.example.EyeCareHubDB.service.PromotionService;

import com.example.EyeCareHubDB.dto.PromotionDTO;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;

@Tag(name = "Promotion")
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<Promotion> create(@RequestBody Promotion promotion) {
        return ResponseEntity.ok(promotionService.createPromotion(promotion));
    }

    @GetMapping
    public ResponseEntity<Page<Promotion>> getActive(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(promotionService.getAllActivePromotions(PageRequest.of(page, size)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promotion> update(@PathVariable("id") Long id, @RequestBody Promotion promotion) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, promotion));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestParam("code") String code, 
                                     @RequestParam(value = "subtotal", defaultValue = "0") java.math.BigDecimal subtotal) {
        try {
            java.util.Optional<com.example.EyeCareHubDB.entity.Promotion> promoOpt = promotionService.validateCode(code, subtotal);
            if (promoOpt.isPresent()) {
                com.example.EyeCareHubDB.entity.Promotion promo = promoOpt.get();
                java.math.BigDecimal discount = promotionService.calculateDiscount(promo, subtotal, new java.math.BigDecimal("30000"));
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("promotion", promotionService.toDTO(promo));
                response.put("discountAmount", discount);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Invalid promotion code");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
