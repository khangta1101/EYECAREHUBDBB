package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.EyeCareHubDB.dto.CartDTO;
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
    public CartItemDTO addItem(Long customerId, com.example.EyeCareHubDB.dto.AddToCartRequest request) {
        Long variantId = request.getVariantId();
        int qty = request.getQty();
        Long prescriptionId = request.getPrescriptionId();
        LocalDateTime expectedAt = request.getExpectedAt();
        
        System.out.println("====== CART DEBUG ======");
        System.out.println("CustomerId: " + customerId);
        System.out.println("VariantId: " + variantId);
        System.out.println("Qty: " + qty);

        Cart cart = getOrCreateActiveCart(customerId);
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found with ID: " + variantId));

        System.out.println("Variant found: " + variant.getVariantName() + " (SKU: " + variant.getSku() + ")");

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

        boolean preOrder = request.getIsPreorder() != null ? request.getIsPreorder() : false;

        return cartItemRepository.findByCartAndVariantAndPrescriptionIdAndIsPreorder(cart, variant, prescriptionId, preOrder)
            .map(existing -> {
                existing.setQty(existing.getQty() + qty);
                return toDTO(cartItemRepository.save(existing));
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
                return toDTO(cartItemRepository.save(newItem));
            });
    }

    @Transactional
    public CartItemDTO updateItem(Long cartItemId, int qty) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("CartItem not found"));
        
        if (qty <= 0) {
            cartItemRepository.delete(item);
            return null;
        }
        item.setQty(qty);
        return toDTO(cartItemRepository.save(item));
    }

    @Transactional
    public void removeItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional(readOnly = true)
    public Cart getCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return cartRepository.findByCustomerAndStatus(customer, CartStatus.ACTIVE)
            .orElseThrow(() -> new RuntimeException("No active cart found"));
    }

    @Transactional(readOnly = true)
    public CartDTO getCartDTO(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        return cartRepository.findByCustomerAndStatus(customer, CartStatus.ACTIVE)
            .map(this::toCartDTO)
            .orElseGet(() -> CartDTO.builder()
                .customerId(customerId)
                .status(CartStatus.ACTIVE.name())
                .items(List.of())
                .subtotal(BigDecimal.ZERO)
                .itemCount(0)
                .build());
    }

    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItems(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        return cartRepository.findByCustomerAndStatus(customer, CartStatus.ACTIVE)
            .map(cart -> cart.getItems().stream()
                .map(this::toDTO)
                .toList())
            .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public CartItemDTO getCartItemDTO(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("CartItem not found with id: " + cartItemId));
        return toDTO(item);
    }

    public CartDTO toCartDTO(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
            .map(item -> item.getUnitPriceSnap().multiply(BigDecimal.valueOf(item.getQty())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDTO.builder()
            .id(cart.getId())
            .customerId(cart.getCustomer().getId())
            .status(cart.getStatus().name())
            .items(cart.getItems().stream().map(this::toDTO).toList())
            .subtotal(subtotal)
            .itemCount(cart.getItems().stream().mapToInt(CartItem::getQty).sum())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    public CartItemDTO toDTO(CartItem item) {
        return CartItemDTO.builder()
            .id(item.getId())
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
