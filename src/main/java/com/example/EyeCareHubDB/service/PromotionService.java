package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.Promotion;
import com.example.EyeCareHubDB.entity.Promotion.DiscountType;
import com.example.EyeCareHubDB.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    private String generateUniqueCode() {
        String code;
        do {
            code = "PRM-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (promotionRepository.findByCode(code).isPresent());
        return code;
    }

    public Optional<Promotion> validateCode(String code, BigDecimal orderSubtotal) {
        Promotion promo = promotionRepository.findByCodeAndIsActiveTrue(code)
            .orElseThrow(() -> new RuntimeException("Promotion code not found or inactive: " + code));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promo.getStartAt()) || now.isAfter(promo.getEndAt())) {
            throw new RuntimeException("Promotion code is not valid at this time");
        }
        if (promo.getMinOrderAmount() != null && orderSubtotal.compareTo(promo.getMinOrderAmount()) < 0) {
            throw new RuntimeException("Order subtotal does not meet minimum: " + promo.getMinOrderAmount());
        }
        return Optional.of(promo);
    }

    public BigDecimal calculateDiscount(Promotion promo, BigDecimal subtotal, BigDecimal shippingFee) {
        BigDecimal discount = BigDecimal.ZERO;
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(promo.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else if (promo.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = promo.getDiscountValue();
        } else if (promo.getDiscountType() == DiscountType.FREE_SHIPPING) {
            discount = shippingFee;
        }
        if (promo.getMaxDiscount() != null && discount.compareTo(promo.getMaxDiscount()) > 0) {
            discount = promo.getMaxDiscount();
        }
        return discount.min(subtotal);
    }

    public Promotion createPromotion(Promotion promotion) {
        if (promotion.getCode() == null || promotion.getCode().isEmpty()) {
            promotion.setCode(generateUniqueCode());
        }
        return promotionRepository.save(promotion);
    }

    public Page<Promotion> getAllActivePromotions(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findByIsActiveTrueAndStartAtBeforeAndEndAtAfter(now, now, pageable);
    }

    @Transactional
    public Promotion updatePromotion(Long id, Promotion updated) {
        Promotion promo = promotionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion not found: " + id));
        promo.setCode(updated.getCode());
        promo.setDiscountValue(updated.getDiscountValue());
        promo.setMinOrderAmount(updated.getMinOrderAmount());
        promo.setMaxDiscount(updated.getMaxDiscount());
        promo.setStartAt(updated.getStartAt());
        promo.setEndAt(updated.getEndAt());
        promo.setIsActive(updated.getIsActive());
        return promotionRepository.save(promo);
    }
}
