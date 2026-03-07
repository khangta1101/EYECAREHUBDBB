package com.example.EyeCareHubDB.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.FulfillmentTask;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskStatus;
import com.example.EyeCareHubDB.service.FulfillmentService;

import lombok.RequiredArgsConstructor;

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

    @GetMapping("/my-tasks")
    public ResponseEntity<List<FulfillmentTask>> getMyTasks(@RequestParam Long accountId,
                                                             @RequestParam(defaultValue = "PENDING") TaskStatus status) {
        return ResponseEntity.ok(fulfillmentService.getMyTasks(accountId, status));
    }
}
