package com.example.EyeCareHubDB.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.InventoryLocation;
import com.example.EyeCareHubDB.entity.InventoryStock;
import com.example.EyeCareHubDB.entity.ProductVariant;
import com.example.EyeCareHubDB.repository.InventoryLocationRepository;
import com.example.EyeCareHubDB.repository.InventoryStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VariantInventoryService {

    private static final String DEFAULT_LOCATION_CODE = "DEFAULT";
    private static final String DEFAULT_LOCATION_NAME = "Default Warehouse";

    private final InventoryStockRepository stockRepository;
    private final InventoryLocationRepository locationRepository;

    @Transactional(readOnly = true)
    public VariantStockSnapshot getStockSnapshot(Long variantId) {
        return summarize(stockRepository.findByVariantId(variantId));
    }

    @Transactional(readOnly = true)
    public VariantStockSnapshot getStockSnapshot(ProductVariant variant) {
        return getStockSnapshot(variant.getId());
    }

    @Transactional(readOnly = true)
    public boolean hasAvailableStock(Long variantId, int quantity) {
        validatePositiveQuantity(quantity);
        return getStockSnapshot(variantId).availableQuantity() >= quantity;
    }

    @Transactional
    public void reserveStock(Long variantId, int quantity) {
        validatePositiveQuantity(quantity);

        List<InventoryStock> stocks = getOrderedStocks(variantId);
        int available = stocks.stream()
                .mapToInt(stock -> stock.getOnHandQty() - stock.getReservedQty())
                .sum();
        if (available < quantity) {
            throw new RuntimeException("Not enough stock. Available: " + available + ", requested: " + quantity);
        }

        int remaining = quantity;
        for (InventoryStock stock : stocks) {
            int stockAvailable = stock.getOnHandQty() - stock.getReservedQty();
            if (stockAvailable <= 0) {
                continue;
            }

            int allocated = Math.min(stockAvailable, remaining);
            stock.setReservedQty(stock.getReservedQty() + allocated);
            stockRepository.save(stock);
            remaining -= allocated;

            if (remaining == 0) {
                return;
            }
        }
    }

    @Transactional
    public void releaseStock(Long variantId, int quantity) {
        validatePositiveQuantity(quantity);

        List<InventoryStock> stocks = getOrderedStocks(variantId);
        int totalReserved = stocks.stream().mapToInt(InventoryStock::getReservedQty).sum();
        if (totalReserved < quantity) {
            throw new RuntimeException("Cannot release more reserved stock than available for variant: " + variantId);
        }

        int remaining = quantity;
        for (InventoryStock stock : stocks) {
            if (stock.getReservedQty() <= 0) {
                continue;
            }

            int released = Math.min(stock.getReservedQty(), remaining);
            stock.setReservedQty(stock.getReservedQty() - released);
            stockRepository.save(stock);
            remaining -= released;

            if (remaining == 0) {
                return;
            }
        }
    }

    @Transactional
    public void confirmStock(Long variantId, int quantity) {
        validatePositiveQuantity(quantity);

        List<InventoryStock> stocks = getOrderedStocks(variantId);
        int totalReserved = stocks.stream().mapToInt(InventoryStock::getReservedQty).sum();
        if (totalReserved < quantity) {
            throw new RuntimeException("Insufficient reserved stock for variant: " + variantId);
        }

        int remaining = quantity;
        for (InventoryStock stock : stocks) {
            if (stock.getReservedQty() <= 0) {
                continue;
            }

            int confirmed = Math.min(stock.getReservedQty(), remaining);
            stock.setReservedQty(stock.getReservedQty() - confirmed);
            stock.setOnHandQty(stock.getOnHandQty() - confirmed);
            stockRepository.save(stock);
            remaining -= confirmed;

            if (remaining == 0) {
                return;
            }
        }
    }

    @Transactional
    public void decrementAvailableStock(ProductVariant variant, int quantity) {
        validatePositiveQuantity(quantity);

        List<InventoryStock> stocks = getOrderedStocks(variant.getId());
        int available = stocks.stream()
                .mapToInt(stock -> stock.getOnHandQty() - stock.getReservedQty())
                .sum();
        if (available < quantity) {
            throw new RuntimeException("Insufficient stock for variant: " + variant.getSku());
        }

        int remaining = quantity;
        for (InventoryStock stock : stocks) {
            int stockAvailable = stock.getOnHandQty() - stock.getReservedQty();
            if (stockAvailable <= 0) {
                continue;
            }

            int decremented = Math.min(stockAvailable, remaining);
            stock.setOnHandQty(stock.getOnHandQty() - decremented);
            stockRepository.save(stock);
            remaining -= decremented;

            if (remaining == 0) {
                return;
            }
        }
    }

    @Transactional
    public void incrementStock(ProductVariant variant, int quantity) {
        validatePositiveQuantity(quantity);
        InventoryStock stock = getOrCreateDefaultStock(variant);
        stock.setOnHandQty(stock.getOnHandQty() + quantity);
        stockRepository.save(stock);
    }

    @Transactional
    public void setTotalStock(ProductVariant variant, int desiredStockQuantity) {
        if (desiredStockQuantity < 0) {
            throw new RuntimeException("Stock quantity cannot be negative");
        }

        List<InventoryStock> stocks = getOrderedStocks(variant.getId());
        int currentTotal = stocks.stream().mapToInt(InventoryStock::getOnHandQty).sum();
        if (currentTotal == desiredStockQuantity) {
            return;
        }

        if (currentTotal < desiredStockQuantity) {
            InventoryStock defaultStock = getOrCreateDefaultStock(variant);
            defaultStock.setOnHandQty(defaultStock.getOnHandQty() + (desiredStockQuantity - currentTotal));
            stockRepository.save(defaultStock);
            return;
        }

        int totalReserved = stocks.stream().mapToInt(InventoryStock::getReservedQty).sum();
        if (desiredStockQuantity < totalReserved) {
            throw new RuntimeException("Cannot set stock below reserved quantity for variant: " + variant.getSku());
        }

        int remainingToReduce = currentTotal - desiredStockQuantity;
        for (InventoryStock stock : stocks) {
            int reducible = stock.getOnHandQty() - stock.getReservedQty();
            if (reducible <= 0) {
                continue;
            }

            int reduced = Math.min(reducible, remainingToReduce);
            stock.setOnHandQty(stock.getOnHandQty() - reduced);
            stockRepository.save(stock);
            remainingToReduce -= reduced;

            if (remainingToReduce == 0) {
                return;
            }
        }

        throw new RuntimeException("Unable to reduce stock to requested quantity for variant: " + variant.getSku());
    }

    private VariantStockSnapshot summarize(List<InventoryStock> stocks) {
        int stockQuantity = stocks.stream().mapToInt(InventoryStock::getOnHandQty).sum();
        int reservedQuantity = stocks.stream().mapToInt(InventoryStock::getReservedQty).sum();
        return new VariantStockSnapshot(stockQuantity, reservedQuantity);
    }

    private List<InventoryStock> getOrderedStocks(Long variantId) {
        return new ArrayList<>(stockRepository.findByVariantId(variantId));
    }

    private InventoryStock getOrCreateDefaultStock(ProductVariant variant) {
        InventoryLocation defaultLocation = locationRepository.findByCode(DEFAULT_LOCATION_CODE)
                .orElseGet(this::createDefaultLocation);

        return stockRepository.findByVariantAndLocation(variant, defaultLocation)
                .orElseGet(() -> InventoryStock.builder()
                        .variant(variant)
                        .location(defaultLocation)
                        .onHandQty(0)
                        .reservedQty(0)
                        .build());
    }

    private InventoryLocation createDefaultLocation() {
        return locationRepository.save(InventoryLocation.builder()
                .name(DEFAULT_LOCATION_NAME)
                .code(DEFAULT_LOCATION_CODE)
                .locationType(InventoryLocation.LocationType.WAREHOUSE)
                .isActive(true)
                .build());
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
    }

    public record VariantStockSnapshot(int stockQuantity, int reservedQuantity) {
        public int availableQuantity() {
            return stockQuantity - reservedQuantity;
        }
    }
}