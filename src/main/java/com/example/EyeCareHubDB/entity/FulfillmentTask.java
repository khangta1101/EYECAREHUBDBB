package com.example.EyeCareHubDB.entity;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FulfillmentTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FulfillmentTask {

    @Id
    @Column(name = "FulfillmentTaskId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderItemId")
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "TaskType", nullable = false, length = 30)
    private TaskType taskType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedTo")
    private Account assignedTo;

    @Column(name = "Note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "EvidenceImageUrl", length = 500)
    private String evidenceImageUrl;

    @Column(name = "StartedAt")
    private LocalDateTime startedAt;

    @Column(name = "DoneAt")
    private LocalDateTime doneAt;

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

    public enum TaskType {
        RECEIVE_PREORDER, CUT_LENS, ASSEMBLE, QC, PACK, SHIP
    }

    public enum TaskStatus {
        PENDING, IN_PROGRESS, DONE, CANCELLED
    }
}
