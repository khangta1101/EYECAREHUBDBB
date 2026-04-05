package com.example.EyeCareHubDB.controller;

import java.util.ArrayList;
import java.util.List;

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

import com.example.EyeCareHubDB.dto.FulfillmentTaskDTO;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskStatus;
import com.example.EyeCareHubDB.service.FulfillmentService;

import io.swagger.v3.oas.annotations.tags.Tag;
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
                                                       @RequestParam(value = "status", required = false) TaskStatus status,
                                                       @RequestParam(value = "assignedToId", required = false) Long assignedToId,
                                                       @RequestParam(value = "note", required = false) String note) {
        return ResponseEntity.ok(fulfillmentService.updateTask(taskId, status, assignedToId, note));
    }

    @PatchMapping(value = "/tasks/{taskId}/with-evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FulfillmentTaskDTO> updateTaskWithEvidence(
            @PathVariable("taskId") Long taskId,
            @RequestParam(value = "status", required = false) TaskStatus status,
            @RequestParam(value = "assignedToId", required = false) Long assignedToId,
            @RequestParam(value = "note", required = false) String note,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "evidenceFiles", required = false) List<MultipartFile> evidenceFiles) {
        return ResponseEntity.ok(fulfillmentService.updateTask(
                taskId,
                status,
                assignedToId,
                note,
                combineEvidenceFiles(file, evidenceFiles)));
    }

    @PostMapping(value = "/tasks/{taskId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FulfillmentTaskDTO> uploadEvidence(
            @PathVariable("taskId") Long taskId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(fulfillmentService.updateTask(taskId, null, null, null, List.of(file)));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<FulfillmentTaskDTO>> getMyTasks(@RequestParam("accountId") Long accountId,
                                                             @RequestParam(value = "status", defaultValue = "PENDING") TaskStatus status) {
        return ResponseEntity.ok(fulfillmentService.getMyTasks(accountId, status));
    }

    @PostMapping("/stock-arrival/{variantId}")
    public ResponseEntity<Void> processStockArrival(@PathVariable("variantId") Long variantId,
                                                   @RequestParam("qty") int qty) {
        fulfillmentService.processStockArrival(variantId, qty);
        return ResponseEntity.ok().build();
    }

    private List<MultipartFile> combineEvidenceFiles(MultipartFile file, List<MultipartFile> evidenceFiles) {
        List<MultipartFile> combined = new ArrayList<>();
        if (file != null && !file.isEmpty()) {
            combined.add(file);
        }
        if (evidenceFiles != null && !evidenceFiles.isEmpty()) {
            for (MultipartFile evidenceFile : evidenceFiles) {
                if (evidenceFile != null && !evidenceFile.isEmpty()) {
                    combined.add(evidenceFile);
                }
            }
        }
        return combined;
    }
}
