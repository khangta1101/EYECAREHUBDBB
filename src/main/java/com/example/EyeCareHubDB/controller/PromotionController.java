package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.EyeCareHubDB.entity.Promotion;
import com.example.EyeCareHubDB.service.PromotionService;

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
}
