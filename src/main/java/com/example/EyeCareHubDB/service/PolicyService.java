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
import java.time.LocalDateTime;

// ============================================================
// SERVICE: PolicyService — Quản lý nội dung chính sách của cửa hàng.
// PolicyType: RETURN (chuyên bảo hành), PRIVACY (bảo mật), SHIPPING (giao hàng), v.v.
// slug → tự động sinh từ title (ví dụ: "Chính Sách" → "chinh-sach")
// isActive=true = PUBLISHED (hiển thị công khai)
// ============================================================
@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {
    
    private final PolicyRepository policyRepository;

    // Tự động convert title → slug (URL-friendly, không dấu tiếng Việt, thường hóa, dấu gạch ngang)
    private String toSlug(String input) {
        if (input == null) return "";
        String slug = java.text.Normalizer.normalize(input.toLowerCase(), java.text.Normalizer.Form.NFD);
        slug = slug.replaceAll("[^\\p{ASCII}]", "")
                   .replaceAll("[^a-z0-9\\s]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("^-+|-+$", "");
        return slug;
    }
    
    public Page<PolicyDTO> getAllPolicies(Pageable pageable) {
        return policyRepository.findAll(pageable)
                .map(this::toDTO);
    }
    
    // Chỉ trả về policy đang PUBLISHED (isActive=true) cho phía khách hàng xem.
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
    
    // Tạo policy mới. Slug tự sinh nếu không có. effectiveFrom = lúc tạo (không null).
    public PolicyDTO createPolicy(PolicyCreateRequest request) {
        String slug = (request.getSlug() == null || request.getSlug().isEmpty()) 
                      ? toSlug(request.getTitle()) : request.getSlug();
        
        Policy policy = Policy.builder()
                .type(Policy.PolicyType.valueOf(request.getType()))
                .title(request.getTitle())
                .slug(slug)
                .content(request.getContent())
                .effectiveFrom(LocalDateTime.now()) // Ensure not null
                .isActive("PUBLISHED".equalsIgnoreCase(request.getStatus()))
                .build();
        
        Policy saved = policyRepository.save(policy);
        return toDTO(saved);
    }
    
    public PolicyDTO updatePolicy(Long id, PolicyUpdateRequest request) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
        
        if (request.getTitle() != null) {
            policy.setTitle(request.getTitle());
            if (request.getSlug() == null || request.getSlug().isEmpty()) {
                policy.setSlug(toSlug(request.getTitle()));
            }
        }
        if (request.getSlug() != null && !request.getSlug().isEmpty()) {
            policy.setSlug(request.getSlug());
        }
        if (request.getContent() != null) {
            policy.setContent(request.getContent());
        }
        if (request.getStatus() != null) {
            policy.setIsActive("PUBLISHED".equalsIgnoreCase(request.getStatus()));
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
    
    // isActive=true → PUBLISHED (hiển thị công khai trời)
    public void publishPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
        
        policy.setIsActive(true);
        policyRepository.save(policy);
    }
    
    // isActive=false → DRAFT (không hiển thị công khai)
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
                .slug(policy.getSlug())
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
