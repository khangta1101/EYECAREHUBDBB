package com.example.EyeCareHubDB.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.InventoryLocation;
import com.example.EyeCareHubDB.entity.InventoryStock;
import com.example.EyeCareHubDB.service.InventoryService;

import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<List<InventoryStock>> getStockByVariant(@PathVariable Long variantId) {
        return ResponseEntity.ok(inventoryService.getStockByVariant(variantId));
    }

    @GetMapping("/stock/location/{locationId}")
    public ResponseEntity<List<InventoryStock>> getStockByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(inventoryService.getStockByLocation(locationId));
    }

    @PatchMapping("/stock/adjust")
    public ResponseEntity<InventoryStock> adjustStock(@RequestParam Long locationId,
                                                       @RequestParam Long variantId,
                                                       @RequestParam int delta) {
        return ResponseEntity.ok(inventoryService.adjustStock(locationId, variantId, delta));
    }
}
