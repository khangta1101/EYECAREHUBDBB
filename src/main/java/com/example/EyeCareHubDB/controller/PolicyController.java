package com.example.EyeCareHubDB.controller;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.EyeCareHubDB.dto.PolicyCreateRequest;
import com.example.EyeCareHubDB.dto.PolicyDTO;
import com.example.EyeCareHubDB.dto.PolicyPublicResponse;
import com.example.EyeCareHubDB.dto.PolicyUpdateRequest;
import com.example.EyeCareHubDB.entity.Policy;
import com.example.EyeCareHubDB.service.PolicyService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "Policy Management APIs")
public class PolicyController {
    
    private final PolicyService policyService;
    
    @GetMapping
    public ResponseEntity<Page<PolicyDTO>> getAllPolicies(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(policyService.getAllPolicies(PageRequest.of(page, size)));
    }
    
    @GetMapping("/published")
    public ResponseEntity<Page<PolicyDTO>> getPublishedPolicies(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(policyService.getPublishedPolicies(PageRequest.of(page, size)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PolicyDTO> getPolicyById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<PolicyDTO> getPolicyByType(@PathVariable("type") String type) {
        return ResponseEntity.ok(policyService.getPolicyByType(Policy.PolicyType.valueOf(type)));
    }
    
    @GetMapping("/public/type/{type}")
    public ResponseEntity<PolicyPublicResponse> getPublishedPolicyByType(@PathVariable("type") String type) {
        return ResponseEntity.ok(policyService.getPublishedPolicyByType(Policy.PolicyType.valueOf(type)));
    }
    
    
    @PostMapping
    public ResponseEntity<PolicyDTO> createPolicy(@RequestBody PolicyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.createPolicy(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PolicyDTO> updatePolicy(
            @PathVariable("id") Long id,
            @RequestBody PolicyUpdateRequest request) {
        return ResponseEntity.ok(policyService.updatePolicy(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable("id") Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
