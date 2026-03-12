package com.example.EyeCareHubDB.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Shipment;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
    org.springframework.data.domain.Page<Shipment> findAll(org.springframework.data.domain.Pageable pageable);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
