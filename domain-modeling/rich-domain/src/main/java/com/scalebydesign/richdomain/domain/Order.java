package com.scalebydesign.richdomain.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RICH DOMAIN MODEL
 * 
 * The entity contains BOTH data AND behavior.
 * It protects its own invariants — no external code can put it in an invalid state.
 * 
 * Key characteristics:
 * - State transitions are METHODS (not setters)
 * - Validation happens INSIDE the entity
 * - Business rules are co-located with the data they operate on
 * - Entity is self-consistent at all times
 */
public class Order {

    private UUID id;
    private String customerId;
    private List<LineItem> items;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // ONLY way to create an Order — through a controlled constructor
    public Order(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    // --- BEHAVIOR (business rules enforced here) ---

    public void addItem(String productId, String name, int quantity, BigDecimal unitPrice) {
        assertState(OrderStatus.DRAFT, "Cannot add items to a non-draft order");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Price must be positive");

        // Business rule: merge if same product already in cart
        items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.increaseQuantity(quantity),
                        () -> items.add(new LineItem(productId, name, quantity, unitPrice))
                );
    }

    public void removeItem(String productId) {
        assertState(OrderStatus.DRAFT, "Cannot modify a non-draft order");
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
        if (!removed) throw new IllegalStateException("Item not in order: " + productId);
    }

    public void submit() {
        assertState(OrderStatus.DRAFT, "Only draft orders can be submitted");
        if (items.isEmpty()) throw new IllegalStateException("Cannot submit an empty order");
        this.status = OrderStatus.SUBMITTED;
    }

    public void approve() {
        assertState(OrderStatus.SUBMITTED, "Only submitted orders can be approved");
        this.status = OrderStatus.APPROVED;
    }

    public void complete() {
        assertState(OrderStatus.APPROVED, "Only approved orders can be completed");
        this.status = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // --- CALCULATIONS (domain logic) ---

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(LineItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(LineItem::getQuantity).sum();
    }

    // --- PRIVATE INVARIANT ENFORCEMENT ---

    private void assertState(OrderStatus expected, String message) {
        if (this.status != expected) {
            throw new IllegalStateException(message + ". Current state: " + status);
        }
    }

    // --- GETTERS (no setters for status — only transitions) ---

    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<LineItem> getItems() { return List.copyOf(items); }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    // For persistence reconstruction only
    public void setId(UUID id) { this.id = id; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setItems(List<LineItem> items) { this.items = new ArrayList<>(items); }
}
