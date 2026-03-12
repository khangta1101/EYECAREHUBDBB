package com.example.EyeCareHubDB.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyUpdateRequest {
    private String title;
    private String slug;
    private String content;
    private String status;
    private Boolean isPublished;
}
