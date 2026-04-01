package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

// ============================================================
// ENTITY: Promotion — Mã khuyến mãi (voucher/coupon).
// DiscountType: PERCENTAGE (giảm %) | FIXED_AMOUNT (giảm cố định) | FREE_SHIPPING (miễn ship)
// maxDiscount: giới hạn số tiền giảm tối đa (dùng với PERCENTAGE để tránh giảm quá nhiều).
// minOrderAmount: giá trị đơn tối thiểu để áp dụng mã. ruleJson: cài đặt điều kiện nâng cao (JSON).
// ============================================================
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

    // COUPON=phải nhập mã thủ công. AUTO=tự động áp dụng khi đủ điều kiện.
    public enum PromoType {
        COUPON, AUTO
    }

    // PERCENTAGE: discount = subtotal × value%. FIXED_AMOUNT: giảm cố định. FREE_SHIPPING: giảm =shippingFee.
    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    }
}
