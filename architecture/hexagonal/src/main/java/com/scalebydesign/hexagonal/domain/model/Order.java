package com.scalebydesign.hexagonal.domain.model;

import com.scalebydesign.hexagonal.domain.exception.InvalidOrderStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain Entity - Order
 * 
 * This is the core domain model. It contains business logic and rules.
 * It has ZERO dependencies on frameworks or infrastructure.
 */
public class Order {

    private UUID id;
    private String customerId;
    private List<OrderItem> items;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public Order(String customerId) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    // Business logic lives in the domain model
    public void addItem(String productId, String productName, int quantity, BigDecimal price) {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Cannot add items to an order that is not in CREATED state");
        }
        items.add(new OrderItem(productId, productName, quantity, price));
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        if (items.isEmpty()) {
            throw new InvalidOrderStateException("Cannot confirm an empty order");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void markPaid() {
        if (status != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException("Order must be confirmed before payment");
        }
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status == OrderStatus.PAID) {
            throw new InvalidOrderStateException("Cannot cancel a paid order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // Getters
    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // For persistence reconstruction
    public void setId(UUID id) { this.id = id; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setItems(List<OrderItem> items) { this.items = new ArrayList<>(items); }
}
