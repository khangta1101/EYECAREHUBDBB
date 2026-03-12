package com.example.EyeCareHubDB.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.OrderItem;
import com.example.EyeCareHubDB.entity.Prescription;
import com.example.EyeCareHubDB.repository.OrderItemRepository;
import com.example.EyeCareHubDB.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Prescription createPrescription(Long orderItemId, Prescription prescription) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new RuntimeException("OrderItem not found: " + orderItemId));
        if (!orderItem.getIsPrescription()) {
            throw new RuntimeException("OrderItem is not marked as prescription");
        }
        if (prescriptionRepository.findByOrderItemId(orderItemId).isPresent()) {
            throw new RuntimeException("Prescription already exists for this order item");
        }
        validatePrescriptionValues(prescription);
        prescription.setOrderItem(orderItem);
        return prescriptionRepository.save(prescription);
    }

    private void validatePrescriptionValues(Prescription p) {
        validateRange(p.getSphereOD(), -20.0, 20.0, "OD Sphere");
        validateRange(p.getSphereOS(), -20.0, 20.0, "OS Sphere");
        validateRange(p.getCylOD(), -6.0, 6.0, "OD Cylinder");
        validateRange(p.getCylOS(), -6.0, 6.0, "OS Cylinder");
        validateIntRange(p.getAxisOD(), 0, 180, "OD Axis");
        validateIntRange(p.getAxisOS(), 0, 180, "OS Axis");
    }

    private void validateRange(java.math.BigDecimal val, double min, double max, String field) {
        if (val != null && (val.doubleValue() < min || val.doubleValue() > max)) {
            throw new RuntimeException(field + " must be between " + min + " and " + max);
        }
    }

    private void validateIntRange(Integer val, int min, int max, String field) {
        if (val != null && (val < min || val > max)) {
            throw new RuntimeException(field + " must be between " + min + " and " + max);
        }
    }

    public Prescription getPrescriptionByOrderItem(Long orderItemId) {
        return prescriptionRepository.findByOrderItemId(orderItemId)
            .orElseThrow(() -> new RuntimeException("Prescription not found for order item: " + orderItemId));
    }

    @Transactional
    public Prescription updatePrescription(Long id, Prescription updated) {
        Prescription existing = prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));
        validatePrescriptionValues(updated);
        existing.setSphereOD(updated.getSphereOD());
        existing.setCylOD(updated.getCylOD());
        existing.setAxisOD(updated.getAxisOD());
        existing.setAddOD(updated.getAddOD());
        existing.setSphereOS(updated.getSphereOS());
        existing.setCylOS(updated.getCylOS());
        existing.setAxisOS(updated.getAxisOS());
        existing.setAddOS(updated.getAddOS());
        existing.setPdTotal(updated.getPdTotal());
        existing.setPrescriptionFileUrl(updated.getPrescriptionFileUrl());
        existing.setNotes(updated.getNotes());
        return prescriptionRepository.save(existing);
    }
}
