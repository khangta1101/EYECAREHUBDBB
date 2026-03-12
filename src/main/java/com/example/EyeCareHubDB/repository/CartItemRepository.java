package com.example.EyeCareHubDB.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Cart;
import com.example.EyeCareHubDB.entity.CartItem;
import com.example.EyeCareHubDB.entity.ProductVariant;

import com.example.EyeCareHubDB.entity.CartItemId;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);
}
