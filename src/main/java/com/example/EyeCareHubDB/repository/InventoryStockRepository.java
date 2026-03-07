package com.example.EyeCareHubDB.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.InventoryLocation;
import com.example.EyeCareHubDB.entity.InventoryStock;
import com.example.EyeCareHubDB.entity.ProductVariant;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {
    Optional<InventoryStock> findByVariantAndLocation(ProductVariant variant, InventoryLocation location);
    Optional<InventoryStock> findByVariantIdAndLocationId(Long variantId, Long locationId);
    List<InventoryStock> findByVariantId(Long variantId);
    List<InventoryStock> findByLocationId(Long locationId);
}
