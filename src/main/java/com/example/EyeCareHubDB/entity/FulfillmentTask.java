package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FulfillmentTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FulfillmentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"FulfillmentTaskId\"")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"OrderId\"", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"OrderItemId\"")
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"TaskType\"", nullable = false, length = 30)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"Status\"", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"AssignedTo\"")
    private Account assignedTo;

    @Column(name = "\"Note\"", columnDefinition = "TEXT")
    private String note;

    @Column(name = "\"StartedAt\"")
    private LocalDateTime startedAt;

    @Column(name = "\"DoneAt\"")
    private LocalDateTime doneAt;

    @Column(name = "\"CreatedAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"UpdatedAt\"", nullable = false)
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
