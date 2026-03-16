package com.example.EyeCareHubDB.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private Long id;
    private String recipientName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String ward;
    private String district;
    private String province;
    private String postalCode;
    private String country;
    private Boolean isDefaultShip;
    private Boolean isDefaultBill;
    private LocalDateTime createdAt;
}



