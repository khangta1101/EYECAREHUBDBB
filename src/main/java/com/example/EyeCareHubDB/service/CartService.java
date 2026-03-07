package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.util.List;

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

    @Transactional
    public Cart getOrCreateActiveCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return cartRepository.findByCustomerAndStatus(customer, CartStatus.ACTIVE)
            .orElseGet(() -> cartRepository.save(Cart.builder().customer(customer).status(CartStatus.ACTIVE).build()));
    }

    @Transactional
    public CartItem addItem(Long customerId, Long variantId, int qty) {
        Cart cart = getOrCreateActiveCart(customerId);
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));

        if (variant.getStockQuantity() - variant.getReservedQuantity() < qty) {
            throw new RuntimeException("Not enough stock for variant: " + variantId);
        }

        BigDecimal basePrice = variant.getProduct().getSalePrice() != null
            ? variant.getProduct().getSalePrice()
            : variant.getProduct().getBasePrice();
        BigDecimal additional = variant.getAdditionalPrice() != null ? variant.getAdditionalPrice() : BigDecimal.ZERO;
        BigDecimal price = basePrice.add(additional);

        return cartItemRepository.findByCartAndVariant(cart, variant)
            .map(existing -> {
                existing.setQty(existing.getQty() + qty);
                return cartItemRepository.save(existing);
            })
            .orElseGet(() -> cartItemRepository.save(CartItem.builder()
                .cart(cart).variant(variant).qty(qty).unitPriceSnap(price).build()));
    }

    @Transactional
    public CartItem updateItem(Long cartItemId, int qty) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("CartItem not found: " + cartItemId));
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

    public List<CartItem> getCartItems(Long customerId) {
        return getCart(customerId).getItems();
    }

    @Transactional
    public void markCartOrdered(Cart cart) {
        cart.setStatus(CartStatus.ORDERED);
        cartRepository.save(cart);
    }
}
