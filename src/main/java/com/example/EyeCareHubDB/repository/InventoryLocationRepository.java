package com.example.EyeCareHubDB.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.InventoryLocation;

@Repository
public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, Long> {
    Optional<InventoryLocation> findByCode(String code);
    List<InventoryLocation> findByIsActiveTrue();
}
