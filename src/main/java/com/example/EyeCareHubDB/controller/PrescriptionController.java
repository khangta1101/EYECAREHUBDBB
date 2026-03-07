package com.example.EyeCareHubDB.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.Prescription;
import com.example.EyeCareHubDB.service.PrescriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/order-item/{orderItemId}")
    public ResponseEntity<Prescription> create(@PathVariable Long orderItemId,
                                                @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.createPrescription(orderItemId, prescription));
    }

    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<Prescription> getByOrderItem(@PathVariable Long orderItemId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionByOrderItem(orderItemId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prescription> update(@PathVariable Long id,
                                               @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, prescription));
    }
}
