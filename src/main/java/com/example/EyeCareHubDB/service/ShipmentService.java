package com.example.EyeCareHubDB.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Shipment;
import com.example.EyeCareHubDB.entity.Shipment.ShipmentStatus;
import com.example.EyeCareHubDB.repository.OrderRepository;
import com.example.EyeCareHubDB.repository.ShipmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Shipment createShipment(Long orderId, Shipment shipment) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        shipment.setOrder(order);
        shipment.setStatus(ShipmentStatus.CREATED);
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment updateShipment(Long id, String trackingNumber, String trackingUrl, ShipmentStatus status) {
        Shipment shipment = shipmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Shipment not found: " + id));
        if (trackingNumber != null) shipment.setTrackingNumber(trackingNumber);
        if (trackingUrl != null) shipment.setTrackingUrl(trackingUrl);
        if (status != null) shipment.setStatus(status);
        return shipmentRepository.save(shipment);
    }

    public Shipment getShipmentByOrder(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("No shipment for order: " + orderId));
    }

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }
}
