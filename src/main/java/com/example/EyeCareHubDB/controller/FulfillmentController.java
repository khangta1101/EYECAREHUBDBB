package com.example.EyeCareHubDB.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.EyeCareHubDB.entity.FulfillmentTask;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskStatus;
import com.example.EyeCareHubDB.service.FulfillmentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Fulfillment")
@RestController
@RequestMapping({"/api/fulfillment", "/api/v1/fulfillment", "/fulfillment"})
@RequiredArgsConstructor
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    @PostMapping("/order/{orderId}/generate-tasks")
    public ResponseEntity<Void> generateTasks(@PathVariable Long orderId) {
        fulfillmentService.generateTasksForOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<FulfillmentTask>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(fulfillmentService.getTasksByOrder(orderId));
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<FulfillmentTask> updateTask(@PathVariable Long taskId,
                                                       @RequestParam TaskStatus status,
                                                       @RequestParam(required = false) Long assignedToId) {
        return ResponseEntity.ok(fulfillmentService.updateTask(taskId, status, assignedToId));
    }

    @PostMapping(value = "/tasks/{taskId}/upload-evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadEvidenceImage(@PathVariable("taskId") Long taskId,
                                                                    @RequestPart("file") MultipartFile file) {
        FulfillmentTask task = fulfillmentService.uploadEvidenceImage(taskId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUploadResponse(task));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<FulfillmentTask>> getMyTasks(@RequestParam Long accountId,
                                                             @RequestParam(defaultValue = "PENDING") TaskStatus status) {
        return ResponseEntity.ok(fulfillmentService.getMyTasks(accountId, status));
    }

    private Map<String, Object> toUploadResponse(FulfillmentTask task) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", task.getId());
        body.put("taskType", task.getTaskType() != null ? task.getTaskType().name() : null);
        body.put("status", task.getStatus() != null ? task.getStatus().name() : null);
        body.put("note", task.getNote());
        body.put("evidenceImageUrl", task.getEvidenceImageUrl());
        body.put("startedAt", task.getStartedAt());
        body.put("doneAt", task.getDoneAt());
        body.put("createdAt", task.getCreatedAt());
        body.put("updatedAt", task.getUpdatedAt());
        return body;
    }
}
