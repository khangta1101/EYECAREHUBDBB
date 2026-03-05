package com.example.EyeCareHubDB.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateRequest {
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Long parentId;
    private Integer displayOrder;
}