package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PrescriptionId")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderItemId", nullable = false, unique = true)
    private OrderItem orderItem;

    @Column(name = "PdTotal", precision = 5, scale = 2)
    private BigDecimal pdTotal;

    @Column(name = "PdLeft", precision = 5, scale = 2)
    private BigDecimal pdLeft;

    @Column(name = "PdRight", precision = 5, scale = 2)
    private BigDecimal pdRight;

    // Right eye (OD)
    @Column(name = "SphereOD", precision = 5, scale = 2)
    private BigDecimal sphereOD;

    @Column(name = "CylOD", precision = 5, scale = 2)
    private BigDecimal cylOD;

    @Column(name = "AxisOD")
    private Integer axisOD;

    @Column(name = "AddOD", precision = 5, scale = 2)
    private BigDecimal addOD;

    // Left eye (OS)
    @Column(name = "SphereOS", precision = 5, scale = 2)
    private BigDecimal sphereOS;

    @Column(name = "CylOS", precision = 5, scale = 2)
    private BigDecimal cylOS;

    @Column(name = "AxisOS")
    private Integer axisOS;

    @Column(name = "AddOS", precision = 5, scale = 2)
    private BigDecimal addOS;

    @Column(name = "PrescriptionFileUrl", length = 500)
    private String prescriptionFileUrl;

    @Column(name = "Notes", columnDefinition = "TEXT")
    private String notes;
}
