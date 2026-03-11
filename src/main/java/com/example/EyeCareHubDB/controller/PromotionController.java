package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.Promotion;
import com.example.EyeCareHubDB.service.PromotionService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Promotion")
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<Promotion> create(@RequestBody Promotion promotion) {
        return ResponseEntity.ok(promotionService.createPromotion(promotion));
    }

    @GetMapping
    public ResponseEntity<List<Promotion>> getActive() {
        return ResponseEntity.ok(promotionService.getAllActivePromotions());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promotion> update(@PathVariable Long id, @RequestBody Promotion promotion) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, promotion));
    }
}
