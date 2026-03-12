package com.example.EyeCareHubDB.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.ProductMediaDTO;
import com.example.EyeCareHubDB.entity.Product;
import com.example.EyeCareHubDB.entity.ProductMedia;
import com.example.EyeCareHubDB.entity.ProductVariant;
import com.example.EyeCareHubDB.repository.ProductMediaRepository;
import com.example.EyeCareHubDB.repository.ProductRepository;
import com.example.EyeCareHubDB.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductMediaService {
    
    private final ProductMediaRepository mediaRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    
    public List<ProductMediaDTO> getMediaByProductId(Long productId) {
        return mediaRepository.findProductLevelMediaByProductId(productId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductMediaDTO> getAllMediaByProductId(Long productId) {
        return mediaRepository.findAllMediaByProductId(productId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductMediaDTO> getMediaByVariantId(Long variantId) {
        return mediaRepository.findMediaByVariantId(variantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ProductMediaDTO getMediaById(Long id) {
        return mediaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Product media not found with id: " + id));
    }

    public ProductMediaDTO addMediaByUrl(
            Long productId,
            Long variantId,
            String url,
            String type,
            Integer displayOrder) {
        if (url == null || url.isBlank()) {
            throw new RuntimeException("Url is required");
        }
        return addMedia(productId, variantId, url.trim(), type, displayOrder);
    }
    

    
    public ProductMediaDTO addMedia(
            Long productId,
            Long variantId,
            String fileUrl,
            String type,
            Integer displayOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        ProductVariant variant = null;
        if (variantId != null) {
            variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + variantId));
            if (!variant.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Variant does not belong to product: " + productId);
            }
        }
        
        ProductMedia.ProductMediaBuilder builder = ProductMedia.builder()
                .product(product)
            .variant(variant)
                .url(fileUrl)
                .displayOrder(displayOrder != null ? displayOrder : 0);
        
        if (type != null) {
            try {
                builder.type(ProductMedia.MediaType.valueOf(type.toUpperCase()));
            } catch (Exception e) {
                builder.type(ProductMedia.MediaType.IMAGE);
            }
        }
        
        ProductMedia saved = mediaRepository.save(builder.build());
        return toDTO(saved);
    }
    
    public ProductMediaDTO updateMedia(
            Long id,
            String fileUrl,
            String type,
            Integer displayOrder) {
        ProductMedia media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product media not found with id: " + id));
        
        if (type != null) {
            try {
                media.setType(ProductMedia.MediaType.valueOf(type.toUpperCase()));
            } catch (Exception e) {
                // ignore or default
            }
        }
        if (fileUrl != null) {
            media.setUrl(fileUrl);
        }
        if (displayOrder != null) {
            media.setDisplayOrder(displayOrder);
        }
        
        ProductMedia updated = mediaRepository.save(media);
        return toDTO(updated);
    }

    public ProductMediaDTO updateMediaByUrl(Long id, String url, String type, Integer displayOrder) {
        String normalizedUrl = (url != null && !url.isBlank()) ? url.trim() : null;
        return updateMedia(id, normalizedUrl, type, displayOrder);
    }
    
    public void deleteMedia(Long id) {
        if (!mediaRepository.existsById(id)) {
            throw new RuntimeException("Product media not found with id: " + id);
        }
        mediaRepository.deleteById(id);
    }
    

    
    private ProductMediaDTO toDTO(ProductMedia media) {
        return ProductMediaDTO.builder()
                .id(media.getId())
                .productId(media.getProduct().getId())
                .variantId(media.getVariant() != null ? media.getVariant().getId() : null)
                .type(media.getType().name())
                .url(media.getUrl())
                .displayOrder(media.getDisplayOrder())
                .createdAt(media.getCreatedAt())
                .build();
    }
}
