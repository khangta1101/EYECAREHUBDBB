package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.EyeCareHubDB.dto.CartItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.Cart;
import com.example.EyeCareHubDB.entity.Cart.CartStatus;
import com.example.EyeCareHubDB.entity.CartItem;
import com.example.EyeCareHubDB.entity.Customer;
import com.example.EyeCareHubDB.entity.ProductVariant;
import com.example.EyeCareHubDB.repository.CartItemRepository;
import com.example.EyeCareHubDB.repository.CartRepository;
import com.example.EyeCareHubDB.repository.CustomerRepository;
import com.example.EyeCareHubDB.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductVariantRepository variantRepository;
    private final VariantInventoryService variantInventoryService;

    @Transactional
    public Cart getOrCreateActiveCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return cartRepository.findByCustomerAndStatus(customer, CartStatus.ACTIVE)
            .orElseGet(() -> cartRepository.save(Cart.builder().customer(customer).status(CartStatus.ACTIVE).build()));
    }

    @Transactional
    public CartItem addItem(Long customerId, Long variantId, int qty, 
                            Long prescriptionId, Boolean isPreorder, LocalDateTime expectedAt) {
        Cart cart = getOrCreateActiveCart(customerId);
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));

        if (!variantInventoryService.hasAvailableStock(variantId, qty)) {
            throw new RuntimeException("Not enough stock for variant: " + variantId);
        }

        BigDecimal basePrice = variant.getSalePrice() != null
            ? variant.getSalePrice()
            : variant.getBasePrice();
        
        // Fallback to Product transient prices if variant prices are null (defensive)
        if (basePrice == null) {
            basePrice = variant.getProduct().getSalePrice() != null
                ? variant.getProduct().getSalePrice()
                : variant.getProduct().getBasePrice();
        }

        // Final fallback to zero to avoid NullPointerException
        if (basePrice == null) {
            basePrice = BigDecimal.ZERO;
        }

        BigDecimal additional = variant.getAdditionalPrice() != null ? variant.getAdditionalPrice() : BigDecimal.ZERO;
        BigDecimal price = basePrice.add(additional);

        boolean preOrder = isPreorder != null ? isPreorder : false;

        return cartItemRepository.findByCartAndVariantAndPrescriptionIdAndIsPreorder(cart, variant, prescriptionId, preOrder)
            .map(existing -> {
                existing.setQty(existing.getQty() + qty);
                return cartItemRepository.save(existing);
            })
            .orElseGet(() -> {
                CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .qty(qty)
                    .unitPriceSnap(price)
                    .prescriptionId(prescriptionId)
                    .isPreorder(preOrder)
                    .preorderExpectedAt(expectedAt)
                    .build();
                return cartItemRepository.save(newItem);
            });
    }

    @Transactional
    public CartItem updateItem(Long cartItemId, int qty) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("CartItem not found"));
        
        if (qty <= 0) {
            cartItemRepository.delete(item);
            return null;
        }
        item.setQty(qty);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public Cart getCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return cartRepository.findByCustomerAndStatus(customer, CartStatus.ACTIVE)
            .orElseThrow(() -> new RuntimeException("No active cart found"));
    }

    public List<CartItemDTO> getCartItems(Long customerId) {
        return getCart(customerId).getItems().stream()
            .map(this::toDTO)
            .toList();
    }

    public CartItemDTO toDTO(CartItem item) {
        return CartItemDTO.builder()
            .cartId(item.getCart().getId())
            .variantId(item.getVariant().getId())
            .variantName(item.getVariant().getVariantName())
            .sku(item.getVariant().getSku())
            .qty(item.getQty())
            .unitPrice(item.getUnitPriceSnap())
            .isPreorder(item.getIsPreorder())
            .preorderExpectedAt(item.getPreorderExpectedAt())
            .prescriptionId(item.getPrescriptionId())
            .addedAt(item.getAddedAt())
            .build();
    }

    @Transactional
    public void markCartOrdered(Cart cart) {
        cart.setStatus(CartStatus.ORDERED);
        cartRepository.save(cart);
    }
}
