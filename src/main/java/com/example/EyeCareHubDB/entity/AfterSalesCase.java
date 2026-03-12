package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "after_sales_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfterSalesCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CaseId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "CaseType", nullable = false, length = 20)
    private CaseType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private CaseStatus status = CaseStatus.NEW;

    @Column(name = "Reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ItemsJson", columnDefinition = "TEXT")
    private String itemsJson;

    @Column(name = "EvidenceUrls", columnDefinition = "TEXT")
    private String evidenceUrls;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequestedBy", nullable = false)
    private Account requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HandledBy")
    private Account handledBy;

    @Column(name = "RefundAmount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

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

    public enum CaseType {
        RETURN, WARRANTY, REFUND
    }

    public enum CaseStatus {
        NEW, IN_REVIEW, APPROVED, REJECTED, RESOLVED
    }
}
