package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "\"ProductMedia\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMedia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"MediaId\"")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "\"ProductId\"", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "\"VariantId\"")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductVariant variant;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "\"MediaType\"", nullable = false, length = 20)
    @Builder.Default
    private MediaType type = MediaType.IMAGE;
    
    @Column(name = "\"Url\"", nullable = false, length = 500)
    private String url;
    
    @Transient // Not in DB
    private String altText;
    
    @Transient // Not in DB
    private String title;
    
    @Column(name = "\"SortOrder\"", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Transient // Not in DB
    @Builder.Default
    private Boolean isPrimary = false;
    
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
    
    public enum MediaType {
        IMAGE, VIDEO, DOCUMENT
    }
}
