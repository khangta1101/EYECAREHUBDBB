package com.example.EyeCareHubDB.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
    private String name;
    @JsonAlias("slug")
    private String searchTags;
    private String productType;
    @JsonAlias("categoryId")
    private Long primaryCategoryId;
    private String brand;
    @JsonAlias("shortDescription")
    private String description;
    private Boolean isActive;
}
