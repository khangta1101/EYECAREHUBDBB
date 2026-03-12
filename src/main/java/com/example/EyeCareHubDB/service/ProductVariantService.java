package com.example.EyeCareHubDB.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.ProductVariantCreateRequest;
import com.example.EyeCareHubDB.dto.ProductVariantDTO;
import com.example.EyeCareHubDB.dto.ProductVariantUpdateRequest;
import com.example.EyeCareHubDB.dto.VariantStockResponse;
import com.example.EyeCareHubDB.entity.Product;
import com.example.EyeCareHubDB.entity.ProductVariant;
import com.example.EyeCareHubDB.repository.ProductRepository;
import com.example.EyeCareHubDB.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {
    
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    
    public List<ProductVariantDTO> getVariantsByProductId(Long productId) {
        return variantRepository.findByProductId(productId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductVariantDTO> getActiveVariantsByProductId(Long productId) {
        return variantRepository.findByProductIdAndIsActiveTrue(productId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ProductVariantDTO getVariantById(Long id) {
        return variantRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
    }
    
    public ProductVariantDTO getVariantBySku(String sku) {
        return variantRepository.findBySku(sku)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Product variant not found with sku: " + sku));
    }
    
    public ProductVariantDTO createVariant(Long productId, ProductVariantCreateRequest request) {
        if (variantRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Variant with sku already exists: " + request.getSku());
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .variantName(request.getVariantName())
                .color(request.getColor())
                .size(request.getSize())
                .material(request.getMaterial())
                .attributesJson(request.getAttributesJson())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .basePrice(request.getBasePrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .isActive(true)
                .build();
        
        ProductVariant saved = variantRepository.save(variant);
        return toDTO(saved);
    }
    
    public ProductVariantDTO updateVariant(Long id, ProductVariantUpdateRequest request) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
        
        if (request.getVariantName() != null) {
            variant.setVariantName(request.getVariantName());
        }
        if (request.getColor() != null) {
            variant.setColor(request.getColor());
        }
        if (request.getSize() != null) {
            variant.setSize(request.getSize());
        }
        if (request.getMaterial() != null) {
            variant.setMaterial(request.getMaterial());
        }
        if (request.getAttributesJson() != null) {
            variant.setAttributesJson(request.getAttributesJson());
        }
        if (request.getCurrency() != null) {
            variant.setCurrency(request.getCurrency());
        }
        if (request.getBasePrice() != null) {
            variant.setBasePrice(request.getBasePrice());
        }
        if (request.getSalePrice() != null) {
            variant.setSalePrice(request.getSalePrice());
        }
        if (request.getStockQuantity() != null) {
            variant.setStockQuantity(request.getStockQuantity());
        }
        if (request.getIsActive() != null) {
            variant.setIsActive(request.getIsActive());
        }
        
        ProductVariant updated = variantRepository.save(variant);
        return toDTO(updated);
    }
    
    public void deleteVariant(Long id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
        variant.setIsActive(false);
        variantRepository.save(variant);
    }
    
    public VariantStockResponse getStockStatus(Long id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
        
        return VariantStockResponse.builder()
                .variantId(variant.getId())
                .sku(variant.getSku())
                .stockQuantity(variant.getStockQuantity())
                .reservedQuantity(variant.getReservedQuantity())
                .availableQuantity(variant.getStockQuantity() - variant.getReservedQuantity())
                .build();
    }
    
    public boolean hasStock(Long id, Integer quantity) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
        
        return variant.getStockQuantity() - variant.getReservedQuantity() >= quantity;
    }
    
    public void decrementStock(Long id, Integer quantity) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
        
        if (!hasStock(id, quantity)) {
            throw new RuntimeException("Insufficient stock for variant: " + variant.getSku());
        }
        
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        variantRepository.save(variant);
    }
    
    public void incrementStock(Long id, Integer quantity) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
        
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        variantRepository.save(variant);
    }
    
    private ProductVariantDTO toDTO(ProductVariant variant) {
        return ProductVariantDTO.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .variantName(variant.getVariantName())
                .color(variant.getColor())
                .size(variant.getSize())
                .material(variant.getMaterial())
                .attributesJson(variant.getAttributesJson())
                .currency(variant.getCurrency())
                .basePrice(variant.getBasePrice())
                .salePrice(variant.getSalePrice())
                .stockQuantity(variant.getStockQuantity())
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .build();
    }
}
