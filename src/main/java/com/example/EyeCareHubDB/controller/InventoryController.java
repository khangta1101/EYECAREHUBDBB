package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.InventoryLocation;
import com.example.EyeCareHubDB.entity.InventoryStock;
import com.example.EyeCareHubDB.service.InventoryService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Inventory")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/locations")
    public ResponseEntity<List<InventoryLocation>> getLocations() {
        return ResponseEntity.ok(inventoryService.getAllLocations());
    }

    @PostMapping("/locations")
    public ResponseEntity<InventoryLocation> createLocation(@RequestBody InventoryLocation location) {
        return ResponseEntity.ok(inventoryService.createLocation(location));
    }

    @GetMapping("/stock/variant/{variantId}")
    public ResponseEntity<List<InventoryStock>> getStockByVariant(@PathVariable("variantId") Long variantId) {
        return ResponseEntity.ok(inventoryService.getStockByVariant(variantId));
    }

    @GetMapping("/stock/location/{locationId}")
    public ResponseEntity<List<InventoryStock>> getStockByLocation(@PathVariable("locationId") Long locationId) {
        return ResponseEntity.ok(inventoryService.getStockByLocation(locationId));
    }

    @PatchMapping("/stock/adjust")
    public ResponseEntity<InventoryStock> adjustStock(@RequestParam("locationId") Long locationId,
                                                       @RequestParam("variantId") Long variantId,
                                                       @RequestParam("delta") int delta) {
        return ResponseEntity.ok(inventoryService.adjustStock(locationId, variantId, delta));
    }
}
