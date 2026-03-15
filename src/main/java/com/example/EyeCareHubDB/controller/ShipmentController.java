package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.dto.ShipmentDTO;
import com.example.EyeCareHubDB.entity.Shipment;
import com.example.EyeCareHubDB.entity.Shipment.ShipmentStatus;
import com.example.EyeCareHubDB.service.ShipmentService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Shipment")
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ShipmentDTO> create(@PathVariable("orderId") Long orderId,
                                           @RequestBody Shipment shipment) {
        return ResponseEntity.ok(shipmentService.createShipment(orderId, shipment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentDTO> getByOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrder(orderId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentDTO> update(@PathVariable("id") Long id,
                                           @RequestParam(value = "trackingNumber", required = false) String trackingNumber,
                                           @RequestParam(value = "trackingUrl", required = false) String trackingUrl,
                                           @RequestParam(value = "status", required = false) ShipmentStatus status) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, trackingNumber, trackingUrl, status));
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<ShipmentDTO>> getAll(
            @org.springframework.data.web.PageableDefault(size = 100) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(shipmentService.getAllShipments(pageable));
    }
}
