package com.scalebydesign.state.table.domain;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * STATE MANAGEMENT: Transition Table
 * 
 * All valid transitions are defined in a CENTRALIZED data structure.
 * A single `transitionTo()` method enforces the rules.
 * 
 * The state machine is EXPLICIT — you can read the full table in one place.
 * 
 * Pros:
 * - All transitions visible in one place (self-documenting)
 * - Impossible to accidentally add illegal transitions
 * - Easy to serialize/externalize (load from config/DB)
 * - Great for generating state machine diagrams
 * 
 * Cons:
 * - Transition-specific logic (e.g., "submit requires items") needs
 *   extra handling outside the table
 * - Slightly more abstract than guard methods
 */
public class Order {

    /**
     * THE TRANSITION TABLE
     * 
     * Read as: "From state X, you can go to states {Y, Z}"
     * If a transition is NOT in this map, it's ILLEGAL.
     */
    private static final Map<OrderStatus, Map<OrderEvent, OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.DRAFT, Map.of(
                    OrderEvent.SUBMIT, OrderStatus.SUBMITTED,
                    OrderEvent.CANCEL, OrderStatus.CANCELLED
            ),
            OrderStatus.SUBMITTED, Map.of(
                    OrderEvent.APPROVE, OrderStatus.APPROVED,
                    OrderEvent.CANCEL, OrderStatus.CANCELLED
            ),
            OrderStatus.APPROVED, Map.of(
                    OrderEvent.COMPLETE, OrderStatus.COMPLETED,
                    OrderEvent.CANCEL, OrderStatus.CANCELLED
            ),
            OrderStatus.COMPLETED, Map.of(),  // Terminal state — no transitions out
            OrderStatus.CANCELLED, Map.of()   // Terminal state — no transitions out
    );

    private UUID id;
    private String customerId;
    private OrderStatus status;

    public Order(String customerId) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.status = OrderStatus.DRAFT;
    }

    // --- Public transition methods delegate to the table ---

    public void submit() {
        applyEvent(OrderEvent.SUBMIT);
    }

    public void approve() {
        applyEvent(OrderEvent.APPROVE);
    }

    public void complete() {
        applyEvent(OrderEvent.COMPLETE);
    }

    public void cancel() {
        applyEvent(OrderEvent.CANCEL);
    }

    // --- Core transition logic ---

    private void applyEvent(OrderEvent event) {
        Map<OrderEvent, OrderStatus> allowedTransitions = TRANSITIONS.get(this.status);

        if (allowedTransitions == null || !allowedTransitions.containsKey(event)) {
            throw new IllegalStateException(
                    String.format("Cannot apply event '%s' in state '%s'. Allowed events: %s",
                            event, status, allowedTransitions != null ? allowedTransitions.keySet() : "none")
            );
        }

        OrderStatus previousStatus = this.status;
        this.status = allowedTransitions.get(event);

        // Log the transition (could also fire domain events here)
        System.out.printf("Order %s: %s -[%s]-> %s%n", id, previousStatus, event, this.status);
    }

    // --- Query: what transitions are available? ---

    public Set<OrderEvent> getAvailableEvents() {
        Map<OrderEvent, OrderStatus> allowed = TRANSITIONS.get(this.status);
        return allowed != null ? allowed.keySet() : Set.of();
    }

    // Getters
    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
}
