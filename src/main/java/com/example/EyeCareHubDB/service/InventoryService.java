package com.example.EyeCareHubDB.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.InventoryLocation;
import com.example.EyeCareHubDB.entity.InventoryStock;
import com.example.EyeCareHubDB.entity.ProductVariant;
import com.example.EyeCareHubDB.repository.InventoryLocationRepository;
import com.example.EyeCareHubDB.repository.InventoryStockRepository;
import com.example.EyeCareHubDB.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryStockRepository stockRepository;
    private final InventoryLocationRepository locationRepository;
    private final ProductVariantRepository variantRepository;
    private final VariantInventoryService variantInventoryService;

    @Transactional
    public void reserveStock(Long variantId, int qty) {
        variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));
        variantInventoryService.reserveStock(variantId, qty);
    }

    @Transactional
    public void releaseStock(Long variantId, int qty) {
        variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));
        variantInventoryService.releaseStock(variantId, qty);
    }

    @Transactional
    public void confirmStock(Long variantId, int qty) {
        variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));
        variantInventoryService.confirmStock(variantId, qty);
    }

    public List<InventoryStock> getStockByVariant(Long variantId) {
        return stockRepository.findByVariantId(variantId);
    }

    public List<InventoryStock> getStockByLocation(Long locationId) {
        return stockRepository.findByLocationId(locationId);
    }

    public List<InventoryLocation> getAllLocations() {
        return locationRepository.findByIsActiveTrue();
    }

    @Transactional
    public InventoryLocation createLocation(InventoryLocation location) {
        return locationRepository.save(location);
    }

    @Transactional
    public InventoryStock adjustStock(Long locationId, Long variantId, int onHandDelta) {
        InventoryLocation location = locationRepository.findById(locationId)
            .orElseThrow(() -> new RuntimeException("Location not found: " + locationId));
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));

        InventoryStock stock = stockRepository.findByVariantAndLocation(variant, location)
            .orElseGet(() -> InventoryStock.builder().variant(variant).location(location).onHandQty(0).reservedQty(0).build());

        stock.setOnHandQty(stock.getOnHandQty() + onHandDelta);
        return stockRepository.save(stock);
    }
}
