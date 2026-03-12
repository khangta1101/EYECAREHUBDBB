package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.AfterSalesCase;
import com.example.EyeCareHubDB.entity.AfterSalesCase.CaseStatus;
import com.example.EyeCareHubDB.service.AfterSalesService;

import lombok.RequiredArgsConstructor;

@Tag(name = "After Sales")
@RestController
@RequestMapping("/api/v1/after-sales")
@RequiredArgsConstructor
public class AfterSalesController {

    private final AfterSalesService afterSalesService;

    @PostMapping("/order/{orderId}")
    public ResponseEntity<AfterSalesCase> createCase(@PathVariable Long orderId,
                                                      @RequestBody AfterSalesCase afterSalesCase) {
        return ResponseEntity.ok(afterSalesService.createCase(orderId, afterSalesCase));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfterSalesCase> getCase(@PathVariable Long id) {
        return ResponseEntity.ok(afterSalesService.getCase(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<AfterSalesCase>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(afterSalesService.getCasesByOrder(orderId));
    }

    @GetMapping
    public ResponseEntity<List<AfterSalesCase>> getByStatus(@RequestParam(required = false) CaseStatus status) {
        return ResponseEntity.ok(afterSalesService.getCasesByStatus(status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AfterSalesCase> updateCase(@PathVariable Long id,
                                                      @RequestParam CaseStatus status) {
        return ResponseEntity.ok(afterSalesService.updateCase(id, status));
    }
}
