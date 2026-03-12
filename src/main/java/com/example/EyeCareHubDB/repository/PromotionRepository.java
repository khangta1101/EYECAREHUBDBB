package com.example.EyeCareHubDB.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCodeAndIsActiveTrue(String code);
    Optional<Promotion> findByCode(String code);
    Page<Promotion> findByIsActiveTrueAndStartAtBeforeAndEndAtAfter(
        LocalDateTime start, LocalDateTime end, Pageable pageable);
}
