package com.example.EyeCareHubDB.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
import com.example.EyeCareHubDB.service.VariantInventoryService.VariantStockSnapshot;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {
    
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final VariantInventoryService variantInventoryService;
    
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
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        String sku = resolveSku(product, request);
        
        ProductVariant variant = ProductVariant.builder()
                .product(product)
            .sku(sku)
                .variantName(request.getVariantName())
                .color(request.getColor())
                .size(request.getSize())
                .material(request.getMaterial())
                .attributesJson(request.getAttributesJson())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .basePrice(request.getBasePrice())
                .salePrice(request.getSalePrice())
                .isActive(true)
                .build();
        
        ProductVariant saved = variantRepository.save(variant);
        if (request.getStockQuantity() != null) {
            variantInventoryService.setTotalStock(saved, request.getStockQuantity());
        }
        return toDTO(saved);
    }

    private String resolveSku(Product product, ProductVariantCreateRequest request) {
        if (request.getSku() != null && !request.getSku().isBlank()) {
            String normalized = request.getSku().trim().toUpperCase();
            if (variantRepository.existsBySku(normalized)) {
                throw new RuntimeException("Variant with sku already exists: " + normalized);
            }
            return normalized;
        }
        return generateUniqueSku(product, request);
    }

    private String generateUniqueSku(Product product, ProductVariantCreateRequest request) {
        String baseSku = product.getSku() != null ? product.getSku() : "P" + product.getId();
        StringBuilder sb = new StringBuilder(baseSku);
        
        if (request.getColor() != null && !request.getColor().isBlank()) {
            sb.append("-").append(request.getColor().substring(0, Math.min(3, request.getColor().length())).toUpperCase());
        }
        if (request.getSize() != null && !request.getSize().isBlank()) {
            sb.append("-").append(request.getSize().toUpperCase());
        }
        
        String prefix = sb.toString();
        String candidate = prefix;
        
        if (!variantRepository.existsBySku(candidate)) {
            return candidate;
        }

        // Add random suffix if collision
        for (int i = 0; i < 10; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(10, 100);
            candidate = prefix + "-" + suffix;
            if (!variantRepository.existsBySku(candidate)) {
                return candidate;
            }
        }

        return prefix + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
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
        if (request.getIsActive() != null) {
            variant.setIsActive(request.getIsActive());
        }
        
        ProductVariant updated = variantRepository.save(variant);
        if (request.getStockQuantity() != null) {
            variantInventoryService.setTotalStock(updated, request.getStockQuantity());
        }
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
        VariantStockSnapshot stockSnapshot = variantInventoryService.getStockSnapshot(variant);
        
        return VariantStockResponse.builder()
                .variantId(variant.getId())
                .sku(variant.getSku())
            .stockQuantity(stockSnapshot.stockQuantity())
            .reservedQuantity(stockSnapshot.reservedQuantity())
            .availableQuantity(stockSnapshot.availableQuantity())
                .build();
    }
    
    public boolean hasStock(Long id, Integer quantity) {
        return variantInventoryService.hasAvailableStock(id, quantity);
    }
    
    public void decrementStock(Long id, Integer quantity) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));

        variantInventoryService.decrementAvailableStock(variant, quantity);
    }
    
    public void incrementStock(Long id, Integer quantity) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));

        variantInventoryService.incrementStock(variant, quantity);
    }
    
    private ProductVariantDTO toDTO(ProductVariant variant) {
        VariantStockSnapshot stockSnapshot = variantInventoryService.getStockSnapshot(variant);

        return ProductVariantDTO.builder()
                .variantId(variant.getId())
                .productId(variant.getProduct().getId())
                .sku(variant.getSku())
                .variantName(variant.getVariantName())
                .color(variant.getColor())
                .size(variant.getSize())
                .material(variant.getMaterial())
                .attributesJson(variant.getAttributesJson())
                .currency(variant.getCurrency())
                .basePrice(variant.getBasePrice())
                .salePrice(variant.getSalePrice())
                .stockQuantity(stockSnapshot.stockQuantity())
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .build();
    }
}
