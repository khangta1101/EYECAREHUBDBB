package com.example.EyeCareHubDB.entity;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemId implements Serializable {
    private Long cart;
    private Long variant;
}
