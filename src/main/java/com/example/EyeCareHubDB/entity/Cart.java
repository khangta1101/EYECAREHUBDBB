package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

// ============================================================
// ENTITY: Cart — Giỏ hàng của khách hàng.
// CartStatus: ACTIVE=đang dùng, ORDERED=đã checkout thành đơn hàng, ABANDONED=bỏ dở.
// 1 Customer chỉ có tối đa 1 Cart ACTIVE tại 1 thời điểm.
// items: cascade ALL + orphanRemoval → xóa Cart → xóa luôn CartItem trong DB.
// ============================================================
@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ACTIVE=đang mua sắm, ORDERED=đã tạo đơn hàng, ABANDONED=bỏ giỏ (timeout hoặc bỏ ngang)
    public enum CartStatus {
        ACTIVE, ORDERED, ABANDONED
    }
}
