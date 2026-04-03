package com.example.EyeCareHubDB.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.EyeCareHubDB.entity.FulfillmentTask;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskStatus;
import com.example.EyeCareHubDB.service.FulfillmentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Fulfillment")
@RestController
@RequestMapping("/api/fulfillment")
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

    @PostMapping("/tasks/{taskId}/upload-evidence")
    public ResponseEntity<FulfillmentTask> uploadEvidenceImage(@PathVariable("taskId") Long taskId,
                                                              @RequestParam("image") MultipartFile imageFile) {
        return ResponseEntity.ok(fulfillmentService.uploadEvidenceImage(taskId, imageFile));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<FulfillmentTask>> getMyTasks(@RequestParam Long accountId,
                                                             @RequestParam(defaultValue = "PENDING") TaskStatus status) {
        return ResponseEntity.ok(fulfillmentService.getMyTasks(accountId, status));
    }
}
