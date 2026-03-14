package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.dto.FulfillmentTaskDTO;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskStatus;
import com.example.EyeCareHubDB.service.FulfillmentService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Fulfillment")
@RestController
@RequestMapping("/api/v1/fulfillment")
@RequiredArgsConstructor
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    @PostMapping("/order/{orderId}/generate-tasks")
    public ResponseEntity<Void> generateTasks(@PathVariable("orderId") Long orderId) {
        fulfillmentService.generateTasksForOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<FulfillmentTaskDTO>> getByOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(fulfillmentService.getTasksByOrder(orderId));
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<FulfillmentTaskDTO> updateTask(@PathVariable("taskId") Long taskId,
                                                       @RequestParam("status") TaskStatus status,
                                                       @RequestParam(value = "assignedToId", required = false) Long assignedToId) {
        return ResponseEntity.ok(fulfillmentService.updateTask(taskId, status, assignedToId));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<FulfillmentTaskDTO>> getMyTasks(@RequestParam("accountId") Long accountId,
                                                             @RequestParam(value = "status", defaultValue = "PENDING") TaskStatus status) {
        return ResponseEntity.ok(fulfillmentService.getMyTasks(accountId, status));
    }
}
