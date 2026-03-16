package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "\"Addresses\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"AddressId\"")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;
    
    @Column(name = "\"RecipientName\"", nullable = false, length = 100)
    private String recipientName;
    
    @Column(name = "\"RecipientPhone\"", nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(name = "\"Line1\"", nullable = false, length = 255)
    private String addressLine1;
    
    @Column(name = "\"Line2\"", length = 255)
    private String addressLine2;
    
    @Column(name = "\"Ward\"", length = 100)
    private String ward;

    @Column(name = "\"District\"", length = 100)
    private String district;
    
    @Column(name = "\"Province\"", nullable = false, length = 100)
    private String province;
    
    @Column(name = "\"PostalCode\"", length = 20)
    private String postalCode;
    
    @Column(name = "\"Country\"", nullable = false, length = 50)
    @Builder.Default
    private String country = "Vietnam";
    
    @Column(name = "\"IsDefaultShip\"", nullable = false)
    @Builder.Default
    private Boolean isDefaultShip = false;

    @Column(name = "\"IsDefaultBill\"", nullable = false)
    @Builder.Default
    private Boolean isDefaultBill = false;
    
    @Column(name = "\"CreatedAt\"", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
