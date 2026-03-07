package com.example.EyeCareHubDB.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Cart;
import com.example.EyeCareHubDB.entity.Cart.CartStatus;
import com.example.EyeCareHubDB.entity.Customer;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerAndStatus(Customer customer, CartStatus status);
    List<Cart> findByCustomer(Customer customer);
}
