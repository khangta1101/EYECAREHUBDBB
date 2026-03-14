package com.example.EyeCareHubDB.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.AfterSalesDTO;
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
    public AfterSalesDTO createCase(Long orderId, AfterSalesCase afterSalesCase) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        afterSalesCase.setOrder(order);
        afterSalesCase.setStatus(CaseStatus.NEW);
        return toDTO(caseRepository.save(afterSalesCase));
    }

    @Transactional
    public AfterSalesDTO updateCase(Long id, CaseStatus status) {
        AfterSalesCase asc = caseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Case not found: " + id));
        asc.setStatus(status);
        return toDTO(caseRepository.save(asc));
    }

    @Transactional(readOnly = true)
    public AfterSalesDTO getCase(Long id) {
        return caseRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new RuntimeException("Case not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<AfterSalesDTO> getCasesByOrder(Long orderId) {
        return caseRepository.findByOrderId(orderId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AfterSalesDTO> getAllCases(org.springframework.data.domain.Pageable pageable) {
        return caseRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AfterSalesDTO> getCasesByStatus(CaseStatus status, org.springframework.data.domain.Pageable pageable) {
        return caseRepository.findByStatus(status, pageable).map(this::toDTO);
    }

    private AfterSalesDTO toDTO(AfterSalesCase c) {
        return AfterSalesDTO.builder()
            .id(c.getId())
            .orderId(c.getOrder().getId())
            .orderNo(c.getOrder().getOrderNo())
            .type(c.getType().name())
            .status(c.getStatus().name())
            .reason(c.getReason())
            .itemsJson(c.getItemsJson())
            .evidenceUrls(c.getEvidenceUrls())
            .requestedById(c.getRequestedBy() != null ? c.getRequestedBy().getId() : null)
            .requestedByName(c.getRequestedBy() != null ? c.getRequestedBy().getUsername() : null)
            .handledById(c.getHandledBy() != null ? c.getHandledBy().getId() : null)
            .handledByName(c.getHandledBy() != null ? c.getHandledBy().getUsername() : null)
            .refundAmount(c.getRefundAmount())
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }
}
