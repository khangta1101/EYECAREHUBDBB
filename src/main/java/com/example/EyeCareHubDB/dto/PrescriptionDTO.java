package com.example.EyeCareHubDB.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDTO {
    private Long id;
    private BigDecimal pdTotal;
    private BigDecimal pdLeft;
    private BigDecimal pdRight;
    private BigDecimal sphereOD;
    private BigDecimal cylOD;
    private Integer axisOD;
    private BigDecimal addOD;
    private BigDecimal sphereOS;
    private BigDecimal cylOS;
    private Integer axisOS;
    private BigDecimal addOS;
    private String prescriptionFileUrl;
    private String notes;
}
