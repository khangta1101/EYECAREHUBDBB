package com.example.EyeCareHubDB.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.AfterSalesCase;
import com.example.EyeCareHubDB.entity.AfterSalesCase.CaseStatus;

@Repository
public interface AfterSalesCaseRepository extends JpaRepository<AfterSalesCase, Long> {
    List<AfterSalesCase> findByOrderId(Long orderId);
    org.springframework.data.domain.Page<AfterSalesCase> findAll(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<AfterSalesCase> findByStatus(CaseStatus status, org.springframework.data.domain.Pageable pageable);
    List<AfterSalesCase> findByRequestedById(Long accountId);
}
