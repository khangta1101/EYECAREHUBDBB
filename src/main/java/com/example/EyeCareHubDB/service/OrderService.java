package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.AccountDTO;
import com.example.EyeCareHubDB.dto.AddressDTO;
import com.example.EyeCareHubDB.dto.CustomerDTO;
import com.example.EyeCareHubDB.dto.OrderDTO;
import com.example.EyeCareHubDB.dto.OrderItemDTO;
import com.example.EyeCareHubDB.dto.PrescriptionDTO;
import com.example.EyeCareHubDB.dto.ProductVariantDTO;
import com.example.EyeCareHubDB.dto.PromotionDTO;
import com.example.EyeCareHubDB.entity.Account;
import com.example.EyeCareHubDB.entity.Address;
import com.example.EyeCareHubDB.entity.Cart;
import com.example.EyeCareHubDB.entity.CartItem;
import com.example.EyeCareHubDB.entity.Customer;
import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.entity.Order.OrderStatus;
import com.example.EyeCareHubDB.entity.Order.OrderType;
import com.example.EyeCareHubDB.entity.OrderItem;
import com.example.EyeCareHubDB.entity.Prescription;
import com.example.EyeCareHubDB.entity.ProductVariant;
import com.example.EyeCareHubDB.entity.Promotion;
import com.example.EyeCareHubDB.repository.AddressRepository;
import com.example.EyeCareHubDB.repository.CustomerRepository;
import com.example.EyeCareHubDB.repository.OrderRepository;
import com.example.EyeCareHubDB.service.FulfillmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;
    private final PromotionService promotionService;
    private final InventoryService inventoryService;
    private final FulfillmentService fulfillmentService;

    // Valid status transitions
    private static final Map<OrderStatus, EnumSet<OrderStatus>> VALID_TRANSITIONS;
    static {
        VALID_TRANSITIONS = new HashMap<>();
        VALID_TRANSITIONS.put(OrderStatus.NEW,        EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.AWAITING_STOCK, OrderStatus.LAB_PROCESSING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.CONFIRMED,  EnumSet.of(OrderStatus.PROCESSING, OrderStatus.AWAITING_STOCK, OrderStatus.LAB_PROCESSING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.AWAITING_STOCK, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.LAB_PROCESSING, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.SHIPPED,    EnumSet.of(OrderStatus.COMPLETED));
        VALID_TRANSITIONS.put(OrderStatus.COMPLETED,  EnumSet.of(OrderStatus.REFUNDED));
        VALID_TRANSITIONS.put(OrderStatus.CANCELLED,  EnumSet.noneOf(OrderStatus.class));
        VALID_TRANSITIONS.put(OrderStatus.REFUNDED,   EnumSet.noneOf(OrderStatus.class));
    }


    @Transactional
    public OrderDTO checkout(com.example.EyeCareHubDB.dto.CheckoutRequest request) {
        Long customerId = request.getCustomerId();
        Long addressId = request.getAddressId();
        OrderType orderType = request.getOrderType();
        String promotionCode = request.getPromotionCode();

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        Address shippingAddress = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));

        Cart cart = cartService.getCart(customerId);
        List<CartItem> items = cart.getItems();
        if (items.isEmpty()) throw new RuntimeException("Cart is empty");

        // Calculate subtotal
        BigDecimal subtotal = items.stream()
            .map(item -> item.getUnitPriceSnap().multiply(new BigDecimal(item.getQty())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountTotal = BigDecimal.ZERO;
        Promotion promotion = null;
        BigDecimal shippingFee = new BigDecimal("30000.00");

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
            .shippingAddress(shippingAddress)
            .orderType(orderType)
            .status(OrderStatus.NEW)
            .promotion(promotion)
            .subtotal(subtotal)
            .discountTotal(discountTotal)
            .shippingFee(shippingFee)
            .grandTotal(grandTotal)
            .note(request.getNote())
            .build();

        // Copy cart items -> order items + reserve stock
        Map<Long, com.example.EyeCareHubDB.dto.CheckoutRequest.CheckoutItemRequest> detailMap = new HashMap<>();
        if (request.getItems() != null) {
            for (com.example.EyeCareHubDB.dto.CheckoutRequest.CheckoutItemRequest detail : request.getItems()) {
                detailMap.put(detail.getCartItemId(), detail);
            }
        }

        for (CartItem item : items) {
            com.example.EyeCareHubDB.dto.CheckoutRequest.CheckoutItemRequest detail = detailMap.get(item.getId());

            OrderItem oi = OrderItem.builder()
                .order(order)
                .variant(item.getVariant())
                .qty(item.getQty())
                .unitPrice(item.getUnitPriceSnap())
                .lineTotal(item.getUnitPriceSnap().multiply(BigDecimal.valueOf(item.getQty())))
                .isPrescription(orderType == OrderType.PRESCRIPTION || (detail != null && detail.getPrescription() != null))
                .itemNote(detail != null ? detail.getItemNote() : null)
                .preorderExpectedAt(detail != null ? detail.getPreorderExpectedAt() : null)
                .build();
            order.getItems().add(oi);

            if (orderType == OrderType.IN_STOCK) {
                inventoryService.reserveStock(item.getVariant().getId(), item.getQty());
            }

            // Handle prescription if present
            if (detail != null && detail.getPrescription() != null) {
                Prescription p = Prescription.builder()
                    .sphereOD(detail.getPrescription().getSphereOD())
                    .cylOD(detail.getPrescription().getCylOD())
                    .axisOD(detail.getPrescription().getAxisOD())
                    .addOD(detail.getPrescription().getAddOD())
                    .sphereOS(detail.getPrescription().getSphereOS())
                    .cylOS(detail.getPrescription().getCylOS())
                    .axisOS(detail.getPrescription().getAxisOS())
                    .addOS(detail.getPrescription().getAddOS())
                    .pdTotal(detail.getPrescription().getPdTotal())
                    .pdLeft(detail.getPrescription().getPdLeft())
                    .pdRight(detail.getPrescription().getPdRight())
                    .prescriptionFileUrl(detail.getPrescription().getPrescriptionFileUrl())
                    .notes(detail.getPrescription().getNotes())
                    .orderItem(oi)
                    .build();
                oi.setPrescription(p);
            }
        }

        Order saved = orderRepository.save(order);
        cartService.markCartOrdered(cart);
        return toDTO(saved);
    }

    @Transactional
    public OrderDTO updateStatus(Long orderId, OrderStatus newStatus) {
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
        if (current == OrderStatus.NEW && (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.PROCESSING 
            || newStatus == OrderStatus.AWAITING_STOCK || newStatus == OrderStatus.LAB_PROCESSING)) {
            fulfillmentService.generateTasksForOrder(orderId);
        }
        order.setStatus(newStatus);
        return toDTO(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long id) {
        return orderRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCustomer(Long customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable)
            .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
            .map(this::toDTO);
    }

    private OrderDTO toDTO(Order order) {
        if (order == null) return null;
        return OrderDTO.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .customer(toCustomerDTO(order.getCustomer()))
            .shippingAddress(toAddressDTO(order.getShippingAddress()))
            .salesStaff(toAccountDTO(order.getSalesStaff()))
            .channel(order.getChannel() != null ? order.getChannel().name() : null)
            .orderType(order.getOrderType() != null ? order.getOrderType().name() : null)
            .status(order.getStatus() != null ? order.getStatus().name() : null)
            .promotion(toPromotionDTO(order.getPromotion()))
            .subtotal(order.getSubtotal())
            .discountTotal(order.getDiscountTotal())
            .shippingFee(order.getShippingFee())
            .grandTotal(order.getGrandTotal())
            .note(order.getNote())
            .items(order.getItems().stream().map(this::toOrderItemDTO).collect(Collectors.toList()))
            .createdAt(order.getCreatedAt())
            .build();
    }

    private CustomerDTO toCustomerDTO(Customer customer) {
        if (customer == null) return null;
        return CustomerDTO.builder()
            .id(customer.getId())
            .accountId(customer.getAccount() != null ? customer.getAccount().getId() : null)
            .firstName(customer.getFirstName())
            .lastName(customer.getLastName())
            .gender(customer.getGender() != null ? customer.getGender().name() : null)
            .dateOfBirth(customer.getDateOfBirth())
            .avatarUrl(customer.getAvatarUrl())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }

    private AddressDTO toAddressDTO(Address address) {
        if (address == null) return null;
        return AddressDTO.builder()
            .id(address.getId())
            .recipientName(address.getRecipientName())
            .phoneNumber(address.getPhoneNumber())
            .addressLine1(address.getAddressLine1())
            .addressLine2(address.getAddressLine2())
            .ward(address.getWard())
            .district(address.getDistrict())
            .province(address.getProvince())
            .postalCode(address.getPostalCode())
            .country(address.getCountry())
            .isDefaultShip(address.getIsDefaultShip())
            .isDefaultBill(address.getIsDefaultBill())
            .createdAt(address.getCreatedAt())
            .build();
    }

    private AccountDTO toAccountDTO(Account account) {
        if (account == null) return null;
        return AccountDTO.builder()
            .id(account.getId())
            .email(account.getEmail())
            .phoneNumber(account.getPhoneNumber())
            .role(account.getRole() != null ? account.getRole().name() : null)
            .status(account.getStatus() != null ? account.getStatus().name() : null)
            .createdAt(account.getCreatedAt())
            .updatedAt(account.getUpdatedAt())
            .lastLoginAt(account.getLastLoginAt())
            .build();
    }

    private PromotionDTO toPromotionDTO(Promotion promotion) {
        if (promotion == null) return null;
        String display = "";
        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
            display = "Giảm " + promotion.getDiscountValue().stripTrailingZeros().toPlainString() + "%";
        } else if (promotion.getDiscountType() == Promotion.DiscountType.FIXED_AMOUNT) {
            display = "Giảm " + promotion.getDiscountValue().stripTrailingZeros().toPlainString() + "đ";
        } else if (promotion.getDiscountType() == Promotion.DiscountType.FREE_SHIPPING) {
            display = "Miễn phí vận chuyển";
        }

        return PromotionDTO.builder()
            .id(promotion.getId())
            .code(promotion.getCode())
            .name(promotion.getName())
            .promoType(promotion.getPromoType() != null ? promotion.getPromoType().name() : null)
            .discountType(promotion.getDiscountType() != null ? promotion.getDiscountType().name() : null)
            .discountValue(promotion.getDiscountValue())
            .minOrderAmount(promotion.getMinOrderAmount())
            .maxDiscount(promotion.getMaxDiscount())
            .startAt(promotion.getStartAt())
            .endAt(promotion.getEndAt())
            .ruleJson(promotion.getRuleJson())
            .isActive(promotion.getIsActive())
            .discountDisplay(display)
            .createdAt(promotion.getCreatedAt())
            .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        if (item == null) return null;
        return OrderItemDTO.builder()
            .id(item.getId())
            .variant(toProductVariantDTO(item.getVariant()))
            .qty(item.getQty())
            .unitPrice(item.getUnitPrice())
            .lineTotal(item.getLineTotal())
            .isPrescription(item.getIsPrescription())
            .preorderExpectedAt(item.getPreorderExpectedAt())
            .preorderReceivedAt(item.getPreorderReceivedAt())
            .itemNote(item.getItemNote())
            .prescription(toPrescriptionDTO(item.getPrescription()))
            .build();
    }

    private ProductVariantDTO toProductVariantDTO(ProductVariant variant) {
        if (variant == null) return null;
        return ProductVariantDTO.builder()
            .variantId(variant.getId())
            .productId(variant.getProduct().getId())
            .sku(variant.getSku())
            .variantName(variant.getVariantName())
            .color(variant.getColor())
            .size(variant.getSize())
            .material(variant.getMaterial())
            .attributesJson(variant.getAttributesJson())
            .currency(variant.getCurrency())
            .basePrice(variant.getBasePrice())
            .salePrice(variant.getSalePrice())
            .isActive(variant.getIsActive())
            .createdAt(variant.getCreatedAt())
            .build();
    }

    private PrescriptionDTO toPrescriptionDTO(Prescription prescription) {
        if (prescription == null) return null;
        return PrescriptionDTO.builder()
            .id(prescription.getId())
            .pdTotal(prescription.getPdTotal())
            .pdLeft(prescription.getPdLeft())
            .pdRight(prescription.getPdRight())
            .sphereOD(prescription.getSphereOD())
            .cylOD(prescription.getCylOD())
            .axisOD(prescription.getAxisOD())
            .addOD(prescription.getAddOD())
            .sphereOS(prescription.getSphereOS())
            .cylOS(prescription.getCylOS())
            .axisOS(prescription.getAxisOS())
            .addOS(prescription.getAddOS())
            .prescriptionFileUrl(prescription.getPrescriptionFileUrl())
            .notes(prescription.getNotes())
            .build();
    }
}
