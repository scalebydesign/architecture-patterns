package com.scalebydesign.state.guard.domain;

import java.util.UUID;

/**
 * STATE MANAGEMENT: Guard Methods
 * 
 * Each transition method checks the current state before allowing the change.
 * The state machine is IMPLICIT — encoded in if/throw checks scattered across methods.
 * 
 * Pros:
 * - Simple and readable
 * - No extra classes or data structures
 * - Easy to understand for any Java developer
 * 
 * Cons:
 * - No single place to see ALL valid transitions
 * - Easy to forget a guard in a new method
 * - Hard to visualize the full state machine
 * - Doesn't scale well to 10+ states
 */
public class Order {

    private UUID id;
    private String customerId;
    private OrderStatus status;

    public Order(String customerId) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.status = OrderStatus.DRAFT;
    }

    public void submit() {
        assertState(OrderStatus.DRAFT, "Only draft orders can be submitted");
        this.status = OrderStatus.SUBMITTED;
    }

    public void approve() {
        assertState(OrderStatus.SUBMITTED, "Only submitted orders can be approved");
        this.status = OrderStatus.APPROVED;
    }

    public void complete() {
        assertState(OrderStatus.APPROVED, "Only approved orders can be completed");
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void assertState(OrderStatus expected, String message) {
        if (this.status != expected) {
            throw new IllegalStateException(message + ". Current: " + status);
        }
    }

    // Getters
    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
}
