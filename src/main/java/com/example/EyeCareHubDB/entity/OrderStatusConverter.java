package com.example.EyeCareHubDB.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OrderStatusConverter implements AttributeConverter<Order.OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(Order.OrderStatus attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public Order.OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String normalized = dbData.trim().toUpperCase();
        return switch (normalized) {
            case "WAITING_STOCK", "AWAITING_STOCK", "LAB_PROCESSING" -> Order.OrderStatus.AWAITING;
            case "SHIPPED" -> Order.OrderStatus.DELIVERY;
            default -> Order.OrderStatus.valueOf(normalized);
        };
    }
}
