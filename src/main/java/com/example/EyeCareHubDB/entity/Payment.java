package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"OrderId\"", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentPurpose paymentPurpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProvider provider;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 200)
    private String transactionRef;

    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String rawResponseJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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

    public enum PaymentPurpose {
        DEPOSIT, FINAL, REFUND
    }

    public enum PaymentProvider {
        VNPAY, MOMO, COD, BANK_TRANSFER
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }
}
