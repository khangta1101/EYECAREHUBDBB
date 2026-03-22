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
@RequestMapping("/api/v1/prescriptions")
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
    public ResponseEntity<com.example.EyeCareHubDB.dto.PrescriptionDTO> create(@PathVariable("orderItemId") Long orderItemId,
                                                @RequestBody com.example.EyeCareHubDB.dto.PrescriptionDTO dto) {
        Prescription p = mapToEntity(dto);
        return ResponseEntity.ok(mapToDTO(prescriptionService.createPrescription(orderItemId, p)));
    }

    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<com.example.EyeCareHubDB.dto.PrescriptionDTO> getByOrderItem(@PathVariable("orderItemId") Long orderItemId) {
        return ResponseEntity.ok(mapToDTO(prescriptionService.getPrescriptionByOrderItem(orderItemId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<com.example.EyeCareHubDB.dto.PrescriptionDTO> update(@PathVariable("id") Long id,
                                               @RequestBody com.example.EyeCareHubDB.dto.PrescriptionDTO dto) {
        Prescription p = mapToEntity(dto);
        return ResponseEntity.ok(mapToDTO(prescriptionService.updatePrescription(id, p)));
    }

    private Prescription mapToEntity(com.example.EyeCareHubDB.dto.PrescriptionDTO dto) {
        if (dto == null) return null;
        return Prescription.builder()
            .pdTotal(dto.getPdTotal())
            .pdLeft(dto.getPdLeft())
            .pdRight(dto.getPdRight())
            .sphereOD(dto.getSphereOD())
            .cylOD(dto.getCylOD())
            .axisOD(dto.getAxisOD())
            .addOD(dto.getAddOD())
            .sphereOS(dto.getSphereOS())
            .cylOS(dto.getCylOS())
            .axisOS(dto.getAxisOS())
            .addOS(dto.getAddOS())
            .prescriptionFileUrl(dto.getPrescriptionFileUrl())
            .notes(dto.getNotes())
            .build();
    }

    private com.example.EyeCareHubDB.dto.PrescriptionDTO mapToDTO(Prescription p) {
        if (p == null) return null;
        return com.example.EyeCareHubDB.dto.PrescriptionDTO.builder()
            .id(p.getId())
            .pdTotal(p.getPdTotal())
            .pdLeft(p.getPdLeft())
            .pdRight(p.getPdRight())
            .sphereOD(p.getSphereOD())
            .cylOD(p.getCylOD())
            .axisOD(p.getAxisOD())
            .addOD(p.getAddOD())
            .sphereOS(p.getSphereOS())
            .cylOS(p.getCylOS())
            .axisOS(p.getAxisOS())
            .addOS(p.getAddOS())
            .prescriptionFileUrl(p.getPrescriptionFileUrl())
            .notes(p.getNotes())
            .build();
    }
}
