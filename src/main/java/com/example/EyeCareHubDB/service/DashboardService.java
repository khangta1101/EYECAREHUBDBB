package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.DashboardDTO;
import com.example.EyeCareHubDB.dto.TopProductDTO;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;
import com.example.EyeCareHubDB.repository.CustomerRepository;
import com.example.EyeCareHubDB.repository.OrderItemRepository;
import com.example.EyeCareHubDB.repository.OrderRepository;
import com.example.EyeCareHubDB.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public DashboardDTO getDashboardStats() {
        List<OrderStatus> revenueStatuses = Arrays.asList(
            OrderStatus.CONFIRMED, 
            OrderStatus.PROCESSING, 
            OrderStatus.SHIPPED, 
            OrderStatus.COMPLETED
        );

        BigDecimal totalRevenue = orderRepository.sumTotalRevenueByStatus(revenueStatuses);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        Long totalOrders = orderRepository.count();
        Long totalCustomers = customerRepository.count();
        Long totalProducts = productRepository.count();

        // Count orders by status
        Map<String, Long> statusCounts = new HashMap<>();
        List<Object[]> statusCountData = orderRepository.countOrdersByStatus();
        for (Object[] row : statusCountData) {
            statusCounts.put(((OrderStatus) row[0]).name(), (Long) row[1]);
        }

        // Revenue by status
        Map<String, BigDecimal> revenueByStatus = new HashMap<>();
        List<Object[]> revenueStatusData = orderRepository.sumRevenueByStatus();
        for (Object[] row : revenueStatusData) {
            revenueByStatus.put(((OrderStatus) row[0]).name(), (BigDecimal) row[1]);
        }

        return DashboardDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .orderStatusCounts(statusCounts)
                .revenueByStatus(revenueByStatus)
                .topProducts(getTopProducts(5))
                .build();
    }

    @Transactional(readOnly = true)
    public List<TopProductDTO> getTopProducts(int limit) {
        List<OrderStatus> validStatuses = Arrays.asList(
            OrderStatus.CONFIRMED, 
            OrderStatus.PROCESSING, 
            OrderStatus.SHIPPED, 
            OrderStatus.COMPLETED
        );

        List<Object[]> data = orderItemRepository.findTopSellingProducts(validStatuses, PageRequest.of(0, limit));
        
        return data.stream().map(row -> TopProductDTO.builder()
                .productId((Long) row[0])
                .productName((String) row[1])
                .totalSold((Long) row[2])
                .totalRevenue((BigDecimal) row[3])
                .build()
        ).collect(Collectors.toList());
    }
}
