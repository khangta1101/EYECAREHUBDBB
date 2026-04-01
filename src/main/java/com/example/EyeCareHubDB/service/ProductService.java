package com.example.EyeCareHubDB.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.EyeCareHubDB.dto.ProductCreateRequest;
import com.example.EyeCareHubDB.dto.ProductDTO;
import com.example.EyeCareHubDB.dto.ProductDetailResponse;
import com.example.EyeCareHubDB.dto.ProductUpdateRequest;
import com.example.EyeCareHubDB.dto.CategoryDTO;
import com.example.EyeCareHubDB.entity.Category;
import com.example.EyeCareHubDB.entity.Product;
import com.example.EyeCareHubDB.repository.CategoryRepository;
import com.example.EyeCareHubDB.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// ============================================================
// SERVICE: ProductService — Quản lý toàn bộ nghiệp vụ Sản phẩm.
// ProductType hợp lệ: FRAME (gọng kính), LENS (tròng kính), SERVICE (dịch vụ)
// searchTags = slug URL SEO-friendly (ví dụ: "gong-kinh-rayban-classic")
// SKU = mã sản phẩm duy nhất. Tự sinh nếu không truyền vào.
// Xóa mềm: isActive=false (dữ liệu vẫn còn trong DB để giữ lịch sử đơn hàng)
// ============================================================
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    // Chỉ chấp nhận 3 loại sản phẩm này. Mặc định = FRAME nếu không truyền.
    private static final Set<String> ALLOWED_PRODUCT_TYPES = Set.of("FRAME", "LENS", "SERVICE");
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantService productVariantService;
    private final ProductMediaService productMediaService;
    
    // Lấy tất cả sản phẩm ACTIVE (isActive=true). Sản phẩm xóa mềm không xuất hiện.
    public List<ProductDTO> getAllProducts() {
        return productRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // Lấy chi tiết ĐẦY ĐỦ sản phẩm (fullDescription, basePrice, salePrice, viewCount, soldCount).
    // Dùng cho trang chi tiết sản phẩm trên frontend.
    public ProductDetailResponse getProductDetailById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return toDetailResponse(product);
    }
    
    public ProductDTO getProductBySlug(String slug) {
        return getProductBySearchTags(slug);
    }

    public ProductDetailResponse getProductDetailBySlug(String slug) {
        return getProductDetailBySearchTags(slug);
    }

    // Tìm sản phẩm theo searchTags (slug URL). Dùng cho SEO-friendly URL routing.
    public ProductDTO getProductBySearchTags(String searchTags) {
        return productRepository.findBySearchTags(searchTags)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Product not found with searchTags: " + searchTags));
    }

    public ProductDetailResponse getProductDetailBySearchTags(String searchTags) {
        Product product = productRepository.findBySearchTags(searchTags)
                .orElseThrow(() -> new RuntimeException("Product not found with searchTags: " + searchTags));
        return toDetailResponse(product);
    }
    
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Lấy sản phẩm NỔI BẬT (isFeatured=true). Dùng cho banner trang chủ.
    public List<ProductDTO> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findFeaturedProducts(pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Lấy sản phẩm PHỔ BIẾN (soldCount cao nhất). Dùng cho section "Bán chạy nhất".
    public List<ProductDTO> getPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findPopularProducts(pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Lấy sản phẩm KHUYẾN MÃI (salePrice < basePrice). Dùng cho section "Đang giảm giá".
    public List<ProductDTO> getProductsOnSale(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findProductsOnSale(pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Tìm kiếm sản phẩm theo từ khóa tên. Có phân trang (page, pageSize).
    public List<ProductDTO> searchProducts(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return productRepository.searchByName(keyword, pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Tạo sản phẩm mới. searchTags phải unique. SKU tự sinh nếu không có. isActive mặc định = true.
    public ProductDTO createProduct(ProductCreateRequest request) {
        if (request.getSearchTags() != null && !request.getSearchTags().isBlank()
            && productRepository.existsBySearchTags(request.getSearchTags())) {
            throw new RuntimeException("Product with SearchTags already exists: " + request.getSearchTags());
        }
        
        Category category = categoryRepository.findById(request.getPrimaryCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getPrimaryCategoryId()));
        
        String productType = normalizeProductType(request.getProductType());
        String sku = generateProductSku(request.getName(), request.getSku());
        
        Product product = Product.builder()
                .name(request.getName())
                .sku(sku)
                .searchTags(request.getSearchTags())
                .productType(productType)
                .category(category)
                .brand(request.getBrand())
            .description(request.getDescription())
            .isActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive())
                .build();
        
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }
    

    
    // Cập nhật sản phẩm (partial update — chỉ field nào != null mới cập nhật).
    public ProductDTO updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getSearchTags() != null && !request.getSearchTags().equals(product.getSearchTags())) {
            if (productRepository.existsBySearchTags(request.getSearchTags())) {
                throw new RuntimeException("Product with SearchTags already exists: " + request.getSearchTags());
            }
            product.setSearchTags(request.getSearchTags());
        }
        if (request.getProductType() != null) {
            product.setProductType(normalizeProductType(request.getProductType()));
        }
        if (request.getPrimaryCategoryId() != null) {
            Category category = categoryRepository.findById(request.getPrimaryCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getPrimaryCategoryId()));
            product.setCategory(category);
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }
    
    // Xóa mềm sản phẩm (isActive=false). Không DELETE khỏi DB vì còn lịch sử đơn hàng.
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }
    
    // Chuẩn hóa productType về HOA và kiểm tra hợp lệ. null hoặc rỗng → mặc định "FRAME".
    private String normalizeProductType(String rawType) {
        String productType = rawType;
        if (productType == null || productType.trim().isEmpty()) {
            productType = "FRAME";
        }
        productType = productType.trim().toUpperCase();
        if (!ALLOWED_PRODUCT_TYPES.contains(productType)) {
            throw new RuntimeException("Invalid ProductType. Allowed values: FRAME, LENS, SERVICE");
        }
        return productType;
    }

    // Tự sinh SKU: "P-" + 5 ký tự đầu tên + "-" + 4 số ngẫu nhiên. VD: "P-KINHM-4521".
    private String generateProductSku(String name, String providedSku) {
        if (providedSku != null && !providedSku.isBlank() && !providedSku.equalsIgnoreCase("null")) {
            return providedSku.trim().toUpperCase();
        }
        // Simple generator: Prefix + Name (no accents/spaces) + Random
        String cleanName = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleanName.length() > 5) cleanName = cleanName.substring(0, 5);
        return "P-" + cleanName + "-" + (int)(Math.random() * 9000 + 1000);
    }
    
    // Convert Product → ProductDTO. Bao gồm media (ảnh) và variants (biến thể).
    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .productId(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .searchTags(product.getSearchTags())
                .productType(product.getProductType())
                .primaryCategoryId(product.getCategory().getId())
                .brand(product.getBrand())
                .description(product.getDescription())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .media(productMediaService.getAllMediaByProductId(product.getId()))
                .variants(productVariantService.getVariantsByProductId(product.getId()))
                .build();
    }

    // Convert Product → ProductDetailResponse (đầy đủ hơn ProductDTO).
    // Bổ sung: category chi tiết, fullDescription, basePrice, salePrice, viewCount, soldCount.
    private ProductDetailResponse toDetailResponse(Product product) {
        Category category = product.getCategory();
        CategoryDTO categoryDTO = category == null ? null : CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();

        return ProductDetailResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .slug(product.getSearchTags())
                .sku(product.getSku())
                .category(categoryDTO)
                .brand(product.getBrand())
                .shortDescription(product.getDescription())
                .fullDescription(product.getFullDescription())
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .variants(productVariantService.getVariantsByProductId(product.getId()))
                .media(productMediaService.getAllMediaByProductId(product.getId()))
                .viewCount(product.getViewCount())
                .soldCount(product.getSoldCount())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
