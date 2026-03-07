package com.example.EyeCareHubDB.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.InventoryLocation;
import com.example.EyeCareHubDB.entity.InventoryStock;
import com.example.EyeCareHubDB.entity.ProductVariant;
import com.example.EyeCareHubDB.repository.InventoryLocationRepository;
import com.example.EyeCareHubDB.repository.InventoryStockRepository;
import com.example.EyeCareHubDB.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryStockRepository stockRepository;
    private final InventoryLocationRepository locationRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional
    public void reserveStock(Long variantId, int qty) {
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));
        int available = variant.getStockQuantity() - variant.getReservedQuantity();
        if (available < qty) {
            throw new RuntimeException("Not enough stock. Available: " + available + ", requested: " + qty);
        }
        variant.setReservedQuantity(variant.getReservedQuantity() + qty);
        variantRepository.save(variant);
    }

    @Transactional
    public void releaseStock(Long variantId, int qty) {
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));
        int newReserved = Math.max(0, variant.getReservedQuantity() - qty);
        variant.setReservedQuantity(newReserved);
        variantRepository.save(variant);
    }

    @Transactional
    public void confirmStock(Long variantId, int qty) {
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));
        variant.setReservedQuantity(Math.max(0, variant.getReservedQuantity() - qty));
        variant.setStockQuantity(Math.max(0, variant.getStockQuantity() - qty));
        variantRepository.save(variant);
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
