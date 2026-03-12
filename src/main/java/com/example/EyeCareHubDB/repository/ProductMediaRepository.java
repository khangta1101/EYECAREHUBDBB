package com.example.EyeCareHubDB.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.ProductMedia;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {
    
    @Query("SELECT pm FROM ProductMedia pm WHERE pm.product.id = :productId AND pm.variant.id IS NULL ORDER BY pm.displayOrder")
    List<ProductMedia> findMediaByProductId(@Param("productId") Long productId);
    
    @Query("SELECT pm FROM ProductMedia pm WHERE pm.variant.id = :variantId ORDER BY pm.displayOrder")
    List<ProductMedia> findMediaByVariantId(@Param("variantId") Long variantId);
}
