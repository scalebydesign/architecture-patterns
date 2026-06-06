package com.scalebydesign.cqrs.write;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * WRITE MODEL — Rich domain model used for commands.
 * Contains all business rules and state transitions.
 * Optimized for CORRECTNESS, not queries.
 */
public class Order {

    private UUID id;
    private String customerId;
    private List<LineItem> items;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public Order(String customerId) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(String productId, String name, int quantity, BigDecimal unitPrice) {
        if (status != OrderStatus.DRAFT) throw new IllegalStateException("Cannot modify non-draft order");
        items.add(new LineItem(productId, name, quantity, unitPrice));
    }

    public void submit() {
        if (status != OrderStatus.DRAFT) throw new IllegalStateException("Only draft orders can be submitted");
        if (items.isEmpty()) throw new IllegalStateException("Cannot submit empty order");
        this.status = OrderStatus.SUBMITTED;
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(LineItem::quantity).sum();
    }

    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<LineItem> getItems() { return List.copyOf(items); }

    public record LineItem(String productId, String name, int quantity, BigDecimal unitPrice) {}

    public enum OrderStatus { DRAFT, SUBMITTED, APPROVED, COMPLETED, CANCELLED }
}
