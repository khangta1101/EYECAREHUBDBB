package com.example.EyeCareHubDB.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressCreateRequest {
    private String recipientName;
    private String phoneNumber;
    private String addressLine1;
    private String province;
    private Boolean isDefaultShip;
    private Boolean isDefaultBill;
}
