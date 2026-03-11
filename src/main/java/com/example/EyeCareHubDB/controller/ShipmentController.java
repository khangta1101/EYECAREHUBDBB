package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.Shipment;
import com.example.EyeCareHubDB.entity.Shipment.ShipmentStatus;
import com.example.EyeCareHubDB.service.ShipmentService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Shipment")
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/order/{orderId}")
    public ResponseEntity<Shipment> create(@PathVariable Long orderId,
                                           @RequestBody Shipment shipment) {
        return ResponseEntity.ok(shipmentService.createShipment(orderId, shipment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrder(orderId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Shipment> update(@PathVariable Long id,
                                           @RequestParam(required = false) String trackingNumber,
                                           @RequestParam(required = false) String trackingUrl,
                                           @RequestParam(required = false) ShipmentStatus status) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, trackingNumber, trackingUrl, status));
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> getAll() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }
}
