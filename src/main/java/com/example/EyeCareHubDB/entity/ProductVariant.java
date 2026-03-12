package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"VariantId\"")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "\"ProductId\"", nullable = false)
    private Product product;
    
    @Column(name = "\"SKU\"", nullable = false, unique = true, length = 100)
    private String sku;
    
    @Column(name = "\"VariantName\"", length = 200)
    private String variantName;
    
    @Column(name = "\"Color\"", length = 100)
    private String color;
    
    @Column(name = "\"Size\"", length = 50)
    private String size;
    
    @Column(name = "\"Material\"", length = 100)
    private String material;
    
    @Column(name = "\"AttributesJson\"", length = 2000)
    private String attributesJson;
    
    @Column(name = "\"Currency\"", length = 10)
    @Builder.Default
    private String currency = "VND";
    
    @Column(name = "\"BasePrice\"", precision = 18, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "\"SalePrice\"", precision = 18, scale = 2)
    private BigDecimal salePrice;
    
    @Transient // Not in DB
    private BigDecimal additionalPrice;
    
    @Column(name = "\"StockQuantity\"", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;
    
    @Transient // Not in DB
    @Builder.Default
    private Integer reservedQuantity = 0;
    
    @Transient // Not in DB
    private String imageUrl;
    
    @Column(name = "\"IsActive\"", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Transient // Not in DB
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Column(name = "\"CreatedAt\"", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Transient // Not in DB
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
