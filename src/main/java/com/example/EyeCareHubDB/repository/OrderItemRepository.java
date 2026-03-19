package com.example.EyeCareHubDB.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    List<OrderItem> findByOrderAndIsPrescriptionTrue(Order order);

    @org.springframework.data.jpa.repository.Query("SELECT p.id, p.name, SUM(oi.qty), SUM(oi.lineTotal) " +
           "FROM OrderItem oi JOIN oi.variant v JOIN v.product p " +
           "WHERE oi.order.status IN :statuses " +
           "GROUP BY p.id, p.name " +
           "ORDER BY SUM(oi.qty) DESC")
    List<Object[]> findTopSellingProducts(@org.springframework.data.repository.query.Param("statuses") java.util.Collection<com.example.EyeCareHubDB.entity.Order.OrderStatus> statuses, 
                                          org.springframework.data.domain.Pageable pageable);
}
