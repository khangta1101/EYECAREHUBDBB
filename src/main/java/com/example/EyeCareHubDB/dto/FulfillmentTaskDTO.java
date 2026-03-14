package com.example.EyeCareHubDB.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FulfillmentTaskDTO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long orderItemId;
    private String taskType;
    private String status;
    private Long assignedToId;
    private String assignedToEmail;
    private String note;
    private LocalDateTime startedAt;
    private LocalDateTime doneAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
