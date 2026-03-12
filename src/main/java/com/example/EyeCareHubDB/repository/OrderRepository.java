package com.example.EyeCareHubDB.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Customer;
import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);
}
