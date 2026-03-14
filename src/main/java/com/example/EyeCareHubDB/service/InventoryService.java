package com.example.EyeCareHubDB.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.InventoryLocationDTO;
import com.example.EyeCareHubDB.dto.InventoryStockDTO;
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

    @Transactional(readOnly = true)
    public List<InventoryStockDTO> getStockByVariant(Long variantId) {
        return stockRepository.findByVariantId(variantId).stream().map(this::toStockDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryStockDTO> getStockByLocation(Long locationId) {
        return stockRepository.findByLocationId(locationId).stream().map(this::toStockDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryLocationDTO> getAllLocations() {
        return locationRepository.findByIsActiveTrue().stream().map(this::toLocationDTO).toList();
    }

    @Transactional
    public InventoryLocationDTO createLocation(InventoryLocation location) {
        return toLocationDTO(locationRepository.save(location));
    }

    @Transactional
    public InventoryStockDTO adjustStock(Long locationId, Long variantId, int onHandDelta) {
        InventoryLocation location = locationRepository.findById(locationId)
            .orElseThrow(() -> new RuntimeException("Location not found: " + locationId));
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));

        InventoryStock stock = stockRepository.findByVariantAndLocation(variant, location)
            .orElseGet(() -> InventoryStock.builder().variant(variant).location(location).onHandQty(0).reservedQty(0).build());

        stock.setOnHandQty(stock.getOnHandQty() + onHandDelta);
        return toStockDTO(stockRepository.save(stock));
    }

    public InventoryLocationDTO toLocationDTO(InventoryLocation location) {
        return InventoryLocationDTO.builder()
            .id(location.getId())
            .name(location.getName())
            .code(location.getCode())
            .locationType(location.getLocationType().name())
            .address(location.getAddress())
            .isActive(location.getIsActive())
            .createdAt(location.getCreatedAt())
            .updatedAt(location.getUpdatedAt())
            .build();
    }

    public InventoryStockDTO toStockDTO(InventoryStock stock) {
        return InventoryStockDTO.builder()
            .locationId(stock.getLocation().getId())
            .locationName(stock.getLocation().getName())
            .variantId(stock.getVariant().getId())
            .variantName(stock.getVariant().getVariantName())
            .productNo(stock.getVariant().getProduct() != null ? stock.getVariant().getProduct().getSku() : "")
            .onHandQty(stock.getOnHandQty())
            .reservedQty(stock.getReservedQty())
            .availableQty(stock.getAvailableQty())
            .createdAt(stock.getCreatedAt())
            .updatedAt(stock.getUpdatedAt())
            .build();
    }
}
