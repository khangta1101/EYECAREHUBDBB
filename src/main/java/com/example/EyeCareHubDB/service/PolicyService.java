package com.example.EyeCareHubDB.service;

import com.example.EyeCareHubDB.dto.PolicyDTO;
import com.example.EyeCareHubDB.dto.PolicyCreateRequest;
import com.example.EyeCareHubDB.dto.PolicyUpdateRequest;
import com.example.EyeCareHubDB.dto.PolicyPublicResponse;
import com.example.EyeCareHubDB.entity.Policy;
import com.example.EyeCareHubDB.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {
    
    private final PolicyRepository policyRepository;
    
    public Page<PolicyDTO> getAllPolicies(Pageable pageable) {
        return policyRepository.findAll(pageable)
                .map(this::toDTO);
    }
    
    public Page<PolicyDTO> getPublishedPolicies(Pageable pageable) {
        return policyRepository.findPublishedPolicies(pageable)
                .map(this::toDTO);
    }
    
    public PolicyDTO getPolicyById(Long id) {
        return policyRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
    }
    
    public PolicyDTO getPolicyByType(Policy.PolicyType type) {
        return policyRepository.findByType(type)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Policy not found with type: " + type));
    }
    
    public PolicyPublicResponse getPublishedPolicyByType(Policy.PolicyType type) {
        return policyRepository.findPublishedByType(type)
                .map(this::toPublicResponse)
                .orElseThrow(() -> new RuntimeException("Published policy not found with type: " + type));
    }
    
    public PolicyDTO createPolicy(PolicyCreateRequest request) {
        Policy policy = Policy.builder()
                .type(Policy.PolicyType.valueOf(request.getType()))
                .title(request.getTitle())
                .content(request.getContent())
                .isActive(false)
                .build();
        
        Policy saved = policyRepository.save(policy);
        return toDTO(saved);
    }
    
    public PolicyDTO updatePolicy(Long id, PolicyUpdateRequest request) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
        
        if (request.getTitle() != null) {
            policy.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            policy.setContent(request.getContent());
        }
        if (request.getIsPublished() != null) {
            policy.setIsActive(request.getIsPublished());
        }
        
        Policy updated = policyRepository.save(policy);
        return toDTO(updated);
    }
    
    public void deletePolicy(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new RuntimeException("Policy not found with id: " + id);
        }
        policyRepository.deleteById(id);
    }
    
    public void publishPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
        
        policy.setIsActive(true);
        policyRepository.save(policy);
    }
    
    public void unpublishPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
        
        policy.setIsActive(false);
        policyRepository.save(policy);
    }
    
    private PolicyDTO toDTO(Policy policy) {
        return PolicyDTO.builder()
                .id(policy.getId())
                .type(policy.getType().name())
                .title(policy.getTitle())
                .content(policy.getContent())
                .version(policy.getVersion())
                .effectiveFrom(policy.getEffectiveFrom())
                .effectiveTo(policy.getEffectiveTo())
                .isActive(policy.getIsActive())
                .createdBy(policy.getCreatedBy())
                .createdAt(policy.getCreatedAt())
                .build();
    }
    
    private PolicyPublicResponse toPublicResponse(Policy policy) {
        return PolicyPublicResponse.builder()
                .id(policy.getId())
                .type(policy.getType().name())
                .title(policy.getTitle())
                .content(policy.getContent())
                .build();
    }
}
