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
    public ResponseEntity<AfterSalesCase> createCase(@PathVariable("orderId") Long orderId,
                                                      @RequestBody AfterSalesCase afterSalesCase) {
        return ResponseEntity.ok(afterSalesService.createCase(orderId, afterSalesCase));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfterSalesCase> getCase(@PathVariable("id") Long id) {
        return ResponseEntity.ok(afterSalesService.getCase(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<AfterSalesCase>> getByOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(afterSalesService.getCasesByOrder(orderId));
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<AfterSalesCase>> getAll(
            @RequestParam(name = "status", required = false) CaseStatus status,
            @org.springframework.data.web.PageableDefault(size = 100) org.springframework.data.domain.Pageable pageable) {
        if (status != null) {
            return ResponseEntity.ok(afterSalesService.getCasesByStatus(status, pageable));
        }
        return ResponseEntity.ok(afterSalesService.getAllCases(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AfterSalesCase> updateCase(@PathVariable("id") Long id,
                                                      @RequestParam("status") CaseStatus status) {
        return ResponseEntity.ok(afterSalesService.updateCase(id, status));
    }
}
