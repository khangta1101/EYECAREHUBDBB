package com.example.EyeCareHubDB.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.AfterSalesCase;
import com.example.EyeCareHubDB.entity.AfterSalesCase.CaseStatus;
import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.repository.AfterSalesCaseRepository;
import com.example.EyeCareHubDB.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AfterSalesService {

    private final AfterSalesCaseRepository caseRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public AfterSalesCase createCase(Long orderId, AfterSalesCase afterSalesCase) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        afterSalesCase.setOrder(order);
        afterSalesCase.setStatus(CaseStatus.NEW);
        return caseRepository.save(afterSalesCase);
    }

    @Transactional
    public AfterSalesCase updateCase(Long id, CaseStatus status) {
        AfterSalesCase asc = caseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Case not found: " + id));
        asc.setStatus(status);
        return caseRepository.save(asc);
    }

    public AfterSalesCase getCase(Long id) {
        return caseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Case not found: " + id));
    }

    public List<AfterSalesCase> getCasesByOrder(Long orderId) {
        return caseRepository.findByOrderId(orderId);
    }

    public List<AfterSalesCase> getCasesByStatus(CaseStatus status) {
        return caseRepository.findByStatus(status);
    }
}
