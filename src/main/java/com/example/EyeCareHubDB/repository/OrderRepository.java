package com.example.EyeCareHubDB.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Customer;
import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);
    Page<Order> findByCustomerAndStatusOrderByCreatedAtDesc(Customer customer, OrderStatus status, Pageable pageable);
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);

    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.status IN :statuses")
    java.math.BigDecimal sumTotalRevenueByStatus(@org.springframework.data.repository.query.Param("statuses") java.util.Collection<OrderStatus> statuses);

    @Query("SELECT o.status, COUNT(o.id) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT o.status, SUM(o.grandTotal) FROM Order o GROUP BY o.status")
    List<Object[]> sumRevenueByStatus();
}
