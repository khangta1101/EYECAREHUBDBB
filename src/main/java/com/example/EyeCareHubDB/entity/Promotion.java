package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionId")
    private Long id;

    @Column(name = "Code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "Name", length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "PromoType", nullable = false, length = 20)
    private PromoType promoType;

    @Enumerated(EnumType.STRING)
    @Column(name = "DiscountType", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "DiscountValue", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "MinOrderAmount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "MaxDiscount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "StartAt", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "EndAt", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "RuleJson", columnDefinition = "TEXT")
    private String ruleJson;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PromoType {
        COUPON, AUTO
    }

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    }
}
