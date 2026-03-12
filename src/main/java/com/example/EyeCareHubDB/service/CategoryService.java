package com.example.EyeCareHubDB.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.CategoryCreateRequest;
import com.example.EyeCareHubDB.dto.CategoryDTO;
import com.example.EyeCareHubDB.dto.CategoryUpdateRequest;
import com.example.EyeCareHubDB.entity.Category;
import com.example.EyeCareHubDB.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    public CategoryDTO getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
    }
    
    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CategoryDTO> getSubCategories(Long parentId) {
        return categoryRepository.findSubCategories(parentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public CategoryDTO createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Category with slug already exists: " + request.getSlug());
        }
        
        Category parent = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + request.getParentId()));
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .parent(parent)
                .isActive(true)
                .build();
        
        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }
    
    public CategoryDTO updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlug(request.getSlug())) {
                throw new RuntimeException("Category with slug already exists: " + request.getSlug());
            }
            category.setSlug(request.getSlug());
        }
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        
        Category updated = categoryRepository.save(category);
        return toDTO(updated);
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        category.setIsActive(false);
        categoryRepository.save(category);
    }
    
    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
