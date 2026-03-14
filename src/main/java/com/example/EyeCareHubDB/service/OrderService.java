package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.Cart;
import com.example.EyeCareHubDB.entity.CartItem;
import com.example.EyeCareHubDB.entity.Customer;
import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;
import com.example.EyeCareHubDB.entity.Order.OrderType;
import com.example.EyeCareHubDB.entity.OrderItem;
import com.example.EyeCareHubDB.entity.ProductVariant;
import com.example.EyeCareHubDB.entity.Promotion;
import com.example.EyeCareHubDB.repository.CustomerRepository;
import com.example.EyeCareHubDB.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CartService cartService;
    private final PromotionService promotionService;
    private final InventoryService inventoryService;

    // Valid status transitions
    private static final Map<OrderStatus, EnumSet<OrderStatus>> VALID_TRANSITIONS;
    static {
        VALID_TRANSITIONS = new HashMap<>();
        VALID_TRANSITIONS.put(OrderStatus.NEW,        EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.CONFIRMED,  EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.SHIPPED,    EnumSet.of(OrderStatus.COMPLETED));
        VALID_TRANSITIONS.put(OrderStatus.COMPLETED,  EnumSet.of(OrderStatus.REFUNDED));
        VALID_TRANSITIONS.put(OrderStatus.CANCELLED,  EnumSet.noneOf(OrderStatus.class));
        VALID_TRANSITIONS.put(OrderStatus.REFUNDED,   EnumSet.noneOf(OrderStatus.class));
    }


    @Transactional
    public Order checkout(Long customerId, Long addressId, OrderType orderType,
                          String promotionCode, BigDecimal shippingFee) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        Cart cart = cartService.getCart(customerId);
        List<CartItem> items = cart.getItems();
        if (items.isEmpty()) throw new RuntimeException("Cart is empty");

        // Calculate subtotal
        BigDecimal subtotal = items.stream()
            .map(item -> item.getUnitPriceSnap().multiply(new BigDecimal(item.getQty())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountTotal = BigDecimal.ZERO;
        Promotion promotion = null;

        if (promotionCode != null && !promotionCode.isBlank()) {
            promotion = promotionService.validateCode(promotionCode, subtotal).orElse(null);
            if (promotion != null) {
                discountTotal = promotionService.calculateDiscount(promotion, subtotal, shippingFee);
            }
        }

        BigDecimal grandTotal = subtotal.subtract(discountTotal).add(shippingFee);

        Order order = Order.builder()
            .orderNo("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .customer(customer)
            .orderType(orderType)
            .status(OrderStatus.NEW)
            .promotion(promotion)
            .subtotal(subtotal)
            .discountTotal(discountTotal)
            .shippingFee(shippingFee != null ? shippingFee : BigDecimal.ZERO)
            .grandTotal(grandTotal)
            .build();

        // Copy cart items -> order items + reserve stock
        for (CartItem item : items) {
            OrderItem oi = OrderItem.builder()
                .order(order)
                .variant(item.getVariant())
                .qty(item.getQty())
                .unitPrice(item.getUnitPriceSnap())
                .lineTotal(item.getUnitPriceSnap().multiply(BigDecimal.valueOf(item.getQty())))
                .isPrescription(orderType == OrderType.PRESCRIPTION)
                .build();
            order.getItems().add(oi);

            if (orderType == OrderType.IN_STOCK) {
                inventoryService.reserveStock(item.getVariant().getId(), item.getQty());
            }
        }

        Order saved = orderRepository.save(order);
        cartService.markCartOrdered(cart);
        return saved;
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        OrderStatus current = order.getStatus();
        EnumSet<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new RuntimeException("Cannot transition from " + current + " to " + newStatus);
        }
        if (newStatus == OrderStatus.CANCELLED) {
            order.getItems().forEach(i ->
                inventoryService.releaseStock(i.getVariant().getId(), i.getQty()));
        }
        if (newStatus == OrderStatus.COMPLETED) {
            order.getItems().forEach(i ->
                inventoryService.confirmStock(i.getVariant().getId(), i.getQty()));
        }
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public Page<Order> getOrdersByCustomer(Long customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable);
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
