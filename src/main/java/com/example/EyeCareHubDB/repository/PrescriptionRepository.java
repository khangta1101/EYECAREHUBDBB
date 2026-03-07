package com.example.EyeCareHubDB.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.OrderItem;
import com.example.EyeCareHubDB.entity.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByOrderItem(OrderItem orderItem);
    Optional<Prescription> findByOrderItemId(Long orderItemId);
}
