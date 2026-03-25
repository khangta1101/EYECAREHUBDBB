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

import com.example.EyeCareHubDB.dto.PromotionDTO;

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
        Optional<Promotion> promoOpt = promotionRepository.findByCodeAndIsActiveTrue(code);
        if (promoOpt.isEmpty()) {
            return Optional.empty();
        }
        Promotion promo = promoOpt.get();

        LocalDateTime now = LocalDateTime.now();
        if (promo.getStartAt() != null && now.isBefore(promo.getStartAt())) {
            throw new RuntimeException("Promotion has not started yet");
        }
        if (promo.getEndAt() != null && now.isAfter(promo.getEndAt())) {
            throw new RuntimeException("Promotion code is expired");
        }
        if (promo.getMinOrderAmount() != null && orderSubtotal.compareTo(promo.getMinOrderAmount()) < 0) {
            throw new RuntimeException("Order subtotal does not meet minimum: " + promo.getMinOrderAmount());
        }
        return Optional.of(promo);
    }

    public BigDecimal calculateDiscount(Promotion promo, BigDecimal subtotal, BigDecimal shippingFee) {
        BigDecimal val = promo.getDiscountValue() != null ? promo.getDiscountValue() : BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(val).divide(BigDecimal.valueOf(100));
        } else if (promo.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = val;
        } else if (promo.getDiscountType() == DiscountType.FREE_SHIPPING) {
            discount = shippingFee != null ? shippingFee : BigDecimal.ZERO;
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

    public PromotionDTO toDTO(Promotion promotion) {
        if (promotion == null) return null;
        String display = "";
        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
            display = "Giảm " + promotion.getDiscountValue().stripTrailingZeros().toPlainString() + "%";
        } else if (promotion.getDiscountType() == Promotion.DiscountType.FIXED_AMOUNT) {
            display = "Giảm " + promotion.getDiscountValue().stripTrailingZeros().toPlainString() + "đ";
        } else if (promotion.getDiscountType() == Promotion.DiscountType.FREE_SHIPPING) {
            display = "Miễn phí vận chuyển";
        }

        return PromotionDTO.builder()
            .id(promotion.getId())
            .code(promotion.getCode())
            .name(promotion.getName())
            .promoType(promotion.getPromoType() != null ? promotion.getPromoType().name() : null)
            .discountType(promotion.getDiscountType() != null ? promotion.getDiscountType().name() : null)
            .discountValue(promotion.getDiscountValue())
            .minOrderAmount(promotion.getMinOrderAmount())
            .maxDiscount(promotion.getMaxDiscount())
            .startAt(promotion.getStartAt())
            .endAt(promotion.getEndAt())
            .ruleJson(promotion.getRuleJson())
            .isActive(promotion.getIsActive())
            .discountDisplay(display)
            .createdAt(promotion.getCreatedAt())
            .build();
    }
}
