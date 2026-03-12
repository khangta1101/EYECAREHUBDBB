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

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private static final Set<String> ALLOWED_PRODUCT_TYPES = Set.of("FRAME", "LENS", "SERVICE");
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantService productVariantService;
    private final ProductMediaService productMediaService;
    
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
    
    public List<ProductDTO> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findFeaturedProducts(pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductDTO> getPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findPopularProducts(pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductDTO> getProductsOnSale(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findProductsOnSale(pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductDTO> searchProducts(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return productRepository.searchByName(keyword, pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO createProduct(ProductCreateRequest request) {
        if (request.getSearchTags() != null && !request.getSearchTags().isBlank()
            && productRepository.existsBySearchTags(request.getSearchTags())) {
            throw new RuntimeException("Product with SearchTags already exists: " + request.getSearchTags());
        }
        
        Category category = categoryRepository.findById(request.getPrimaryCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getPrimaryCategoryId()));
        
        String productType = normalizeProductType(request.getProductType());
        
        Product product = Product.builder()
                .name(request.getName())
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
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }
    
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
    
    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .productId(product.getId())
                .name(product.getName())
                .searchTags(product.getSearchTags())
                .productType(product.getProductType())
                .primaryCategoryId(product.getCategory().getId())
                .brand(product.getBrand())
                .description(product.getDescription())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }

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
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSearchTags())
                .sku(null) // product-level SKU not modeled; variants carry SKU
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
