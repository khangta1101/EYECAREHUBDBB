package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ============================================================
// ENTITY: Payment — Thông tin thanh toán cho 1 đơn hàng.
// 1 Order có thể có nhiều Payment (thanh toán cọc + trả nốt).
// transactionRef: mã giao dịch VNPay ("VNP" + paymentId + timestamp), duy nhất.
// rawResponseJson: lưu toàn bộ response từ VNPay để đối soát sau.
// PaymentPurpose: DEPOSIT=đặt cọc, FINAL=thanh toán đủ, REFUND=hoàn tiền.
// ============================================================
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentPurpose", nullable = false, length = 20)
    private PaymentPurpose paymentPurpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "Provider", nullable = false, length = 30)
    private PaymentProvider provider;

    @Column(name = "Amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "Currency", length = 10)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "TransactionRef", length = 200)
    private String transactionRef;

    @Column(name = "PaidAt")
    private LocalDateTime paidAt;

    @Column(name = "RawResponseJson", columnDefinition = "TEXT")
    private String rawResponseJson;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // PaymentPurpose: DEPOSIT=thanh toán cọc trước, FINAL=thanh toán toàn bộ, REFUND=hoàn tiền
    public enum PaymentPurpose {
        DEPOSIT, FINAL, REFUND
    }

    // PaymentProvider: VNPAY=online, MOMO=ví điện tử, COD=tiền mặt, BANK_TRANSFER=chuyển khoản
    public enum PaymentProvider {
        VNPAY, MOMO, COD, BANK_TRANSFER
    }

    // PaymentStatus: PENDING=chờ, PAID=đã thanh toán, FAILED=thất bại, REFUNDED=đã hoàn tiền
    public enum PaymentStatus {
        PENDING, PAID, FAILED, REFUNDED
    }
}
