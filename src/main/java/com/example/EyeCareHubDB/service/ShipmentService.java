package com.example.EyeCareHubDB.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.ShipmentDTO;
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
    public ShipmentDTO createShipment(Long orderId, Shipment shipment) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        shipment.setOrder(order);
        shipment.setStatus(ShipmentStatus.CREATED);
        return toDTO(shipmentRepository.save(shipment));
    }

    @Transactional
    public ShipmentDTO updateShipment(Long id, String trackingNumber, String trackingUrl, ShipmentStatus status) {
        Shipment shipment = shipmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Shipment not found: " + id));
        if (trackingNumber != null) shipment.setTrackingNumber(trackingNumber);
        if (trackingUrl != null) shipment.setTrackingUrl(trackingUrl);
        if (status != null) shipment.setStatus(status);
        return toDTO(shipmentRepository.save(shipment));
    }

    @Transactional(readOnly = true)
    public ShipmentDTO getShipmentByOrder(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
            .map(this::toDTO)
            .orElseThrow(() -> new RuntimeException("No shipment for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ShipmentDTO> getAllShipments(org.springframework.data.domain.Pageable pageable) {
        return shipmentRepository.findAll(pageable).map(this::toDTO);
    }

    public ShipmentDTO toDTO(Shipment shipment) {
        return ShipmentDTO.builder()
            .id(shipment.getId())
            .orderId(shipment.getOrder().getId())
            .orderNo(shipment.getOrder().getOrderNo())
            .carrier(shipment.getCarrier())
            .trackingNumber(shipment.getTrackingNumber())
            .trackingUrl(shipment.getTrackingUrl())
            .status(shipment.getStatus().name())
            .estimatedDelivery(shipment.getEstimatedDelivery())
            .shippedAt(shipment.getShippedAt())
            .actualDelivery(shipment.getActualDelivery())
            .note(shipment.getNote())
            .createdAt(shipment.getCreatedAt())
            .updatedAt(shipment.getUpdatedAt())
            .build();
    }
}
