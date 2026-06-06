package com.scalebydesign.anemicdomain.service;

import com.scalebydesign.anemicdomain.model.LineItem;
import com.scalebydesign.anemicdomain.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ANEMIC DOMAIN MODEL — Service contains ALL business logic
 * 
 * Compare with Rich Domain where Order.submit() enforces its own rules.
 * Here, the service is responsible for EVERYTHING:
 * - Validation
 * - State transitions
 * - Calculations
 * - Invariant enforcement
 * 
 * Problem: Nothing stops someone from doing order.setStatus("COMPLETED") 
 * without going through this service. The entity can't protect itself.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final Map<UUID, Order> store = new HashMap<>();

    public Order createOrder(String customerId) {
        log.info("Creating order for customer: {}", customerId);
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setStatus("DRAFT");
        order.setTotal(BigDecimal.ZERO);

        store.put(order.getId(), order);
        return order;
    }

    public Order addItem(UUID orderId, String productId, String name, int quantity, BigDecimal unitPrice) {
        log.info("Adding item to order: orderId={}, productId={}", orderId, productId);
        Order order = findOrder(orderId);

        // Validation in SERVICE, not entity
        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot add items to a non-draft order");
        }
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Price must be positive");

        LineItem item = new LineItem();
        item.setProductId(productId);
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);

        order.getItems().add(item);
        recalculateTotal(order);  // Calculation in SERVICE

        return order;
    }

    public Order submitOrder(UUID orderId) {
        log.info("Submitting order: {}", orderId);
        Order order = findOrder(orderId);

        // State validation in SERVICE
        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("Only draft orders can be submitted");
        }
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot submit an empty order");
        }

        order.setStatus("SUBMITTED");  // Direct mutation — entity doesn't validate
        return order;
    }

    public Order approveOrder(UUID orderId) {
        log.info("Approving order: {}", orderId);
        Order order = findOrder(orderId);

        if (!"SUBMITTED".equals(order.getStatus())) {
            throw new IllegalStateException("Only submitted orders can be approved");
        }

        order.setStatus("APPROVED");
        return order;
    }

    public Order completeOrder(UUID orderId) {
        log.info("Completing order: {}", orderId);
        Order order = findOrder(orderId);

        if (!"APPROVED".equals(order.getStatus())) {
            throw new IllegalStateException("Only approved orders can be completed");
        }

        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        return order;
    }

    public Order cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = findOrder(orderId);

        if ("COMPLETED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }

        order.setStatus("CANCELLED");
        return order;
    }

    public Order getOrder(UUID orderId) {
        return findOrder(orderId);
    }

    private void recalculateTotal(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);
    }

    private Order findOrder(UUID orderId) {
        Order order = store.get(orderId);
        if (order == null) throw new RuntimeException("Order not found: " + orderId);
        return order;
    }
}
