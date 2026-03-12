package com.example.EyeCareHubDB.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    List<ProductVariant> findByProductId(Long productId);
    
    List<ProductVariant> findByProductIdAndIsActiveTrue(Long productId);
    
    Optional<ProductVariant> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.sku = :sku AND pv.isActive = true")
    Optional<ProductVariant> findActiveBySku(@Param("sku") String sku);
    
}
