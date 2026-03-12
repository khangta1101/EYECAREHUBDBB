package com.example.EyeCareHubDB.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    Optional<Policy> findByType(Policy.PolicyType type);
    
    List<Policy> findByIsActiveTrue();
    
    @Query("SELECT p FROM Policy p WHERE p.isActive = true")
    Page<Policy> findPublishedPolicies(Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.type = :type AND p.isActive = true")
    Optional<Policy> findPublishedByType(@Param("type") Policy.PolicyType type);
    
    boolean existsByType(Policy.PolicyType type);
}
