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

// ============================================================
// SERVICE: ProductVariantService — Quản lý các BIẾN THỂ (Variant) của sản phẩm.
// Mỗi sản phẩm có thể có nhiều variant về: màu, size, chất liệu.
// Ví dụ: Gọng Ray-Ban → [Đen-M, Vàng-M, Đen-L] — mỗi variant có SKU, giá, kho riêng.
// SKU variant: tự sinh từ SKU sản phẩm + màu + size. VD: "P-KINHM-DEN-M".
// stockQuantity trong DTO = tổng onHandQty từ tất cả Location (kho).
// ============================================================
@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {
    
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final VariantInventoryService variantInventoryService;
    
    // Lấy TẤT CẢ biến thể (kể cả inactive) của sản phẩm. Dùng cho admin quản lý.
    public List<ProductVariantDTO> getVariantsByProductId(Long productId) {
        return variantRepository.findByProductId(productId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Lấy biến thể ACTIVE (isActive=true). Dùng cho frontend — chỉ hiển thị loại còn bán.
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
    
    // Tra cứu biến thể theo SKU (mã hàng unique). Dùng khi xử lý đơn hàng hoặc quản lý kho.
    public ProductVariantDTO getVariantBySku(String sku) {
        return variantRepository.findBySku(sku)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Product variant not found with sku: " + sku));
    }
    
    // Tạo biến thể mới. SKU tự sinh nếu không có. Nếu có stockQuantity → nhập kho ngay.
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

    // Xác định SKU: nếu được cung cấp và unique → dùng luôn. Ngược lại → tự sinh.
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

    // Tự sinh SKU: productSku + màu(3 ký tự đầu) + size. Thêm suffix số nếu trùng.
    // Fallback cuối: thêm 4 ký tự UUID ngẫu nhiên.
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

        // Thêm số suffix ngẫu nhiên (10-99) nếu SKU trùng
        for (int i = 0; i < 10; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(10, 100);
            candidate = prefix + "-" + suffix;
            if (!variantRepository.existsBySku(candidate)) {
                return candidate;
            }
        }

        return prefix + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    // Cập nhật biến thể (partial update). Nếu có stockQuantity → đồng bộ kho.
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
    
    // Xóa mềm biến thể (isActive=false). Không xóa DB để giữ lịch sử đơn hàng.
    public void deleteVariant(Long id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
        variant.setIsActive(false);
        variantRepository.save(variant);
    }
    
    // Lấy trạng thái tồn kho: stockQty (onHand), reservedQty, availableQty.
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
    
    // Kiểm tra variant còn đủ số lượng để bán không (availableQty >= quantity).
    public boolean hasStock(Long id, Integer quantity) {
        return variantInventoryService.hasAvailableStock(id, quantity);
    }
    
    // Xuất kho NGAY: trừ trực tiếp onHandQty (không qua reserved). Dùng xuất kho thủ công.
    public void decrementStock(Long id, Integer quantity) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));

        variantInventoryService.decrementAvailableStock(variant, quantity);
    }
    
    // Nhập kho: tăng onHandQty tại Location mặc định.
    public void incrementStock(Long id, Integer quantity) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));

        variantInventoryService.incrementStock(variant, quantity);
    }
    
    // Convert ProductVariant → ProductVariantDTO. Đọc thêm stockSnapshot từ InventoryService.
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
