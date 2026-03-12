package com.example.EyeCareHubDB.entity;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockId implements Serializable {
    private Long location;
    private Long variant;
}
