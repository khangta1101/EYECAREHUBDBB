package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.Prescription;
import com.example.EyeCareHubDB.service.PrescriptionService;
import com.example.EyeCareHubDB.service.FileService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Prescription")
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final FileService fileService;

    @PostMapping("/{id}/upload")
    public ResponseEntity<Prescription> uploadFile(@PathVariable("id") Long id,
                                                   @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        String url = fileService.saveFile(file, "prescriptions");
        Prescription existing = prescriptionService.getPrescriptionById(id);
        existing.setPrescriptionFileUrl(url);
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, existing));
    }

    @PostMapping("/order-item/{orderItemId}")
    public ResponseEntity<Prescription> create(@PathVariable("orderItemId") Long orderItemId,
                                                @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.createPrescription(orderItemId, prescription));
    }

    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<Prescription> getByOrderItem(@PathVariable("orderItemId") Long orderItemId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionByOrderItem(orderItemId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prescription> update(@PathVariable("id") Long id,
                                               @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, prescription));
    }
}
