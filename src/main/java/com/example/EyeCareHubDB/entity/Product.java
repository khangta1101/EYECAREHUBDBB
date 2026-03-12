package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ProductId\"")
    private Long id;
    
    @Column(name = "\"Name\"", nullable = false, length = 200)
    private String name;
    
    @Column(name = "\"SearchTags\"", length = 200)
    private String searchTags;
    
    @Column(name = "\"ProductType\"", length = 100)
    private String productType;
    
    @ManyToOne
    @JoinColumn(name = "\"PrimaryCategoryId\"", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;
    
    @Column(name = "\"Brand\"", length = 100)
    private String brand;

    @Column(name = "\"SKU\"", unique = true, length = 100)
    private String sku;
    
    @Column(name = "\"Description\"", length = 2000)
    private String description;
    
    @Transient
    private String fullDescription;
    
    @Transient
    private BigDecimal basePrice;
    
    @Transient
    private BigDecimal salePrice;
    
    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductVariant> variants = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductMedia> media = new ArrayList<>();
    
    @Builder.Default
    @Column(name = "\"IsActive\"", nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Transient
    private Boolean isFeatured = false;
    
    @Builder.Default
    @Transient
    private Integer viewCount = 0;
    
    @Builder.Default
    @Transient
    private Integer soldCount = 0;
    
    @Transient
    private String metaTitle;
    
    @Transient
    private String metaDescription;
    
    @Transient
    private String metaKeywords;
    
    @Builder.Default
    @Column(name = "\"CreatedAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @Transient
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
