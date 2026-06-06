package com.scalebydesign.eventsourcing.aggregate;

import com.scalebydesign.eventsourcing.event.OrderEvent;
import com.scalebydesign.eventsourcing.event.OrderEvent.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * EVENT SOURCING - Aggregate
 * 
 * The Order doesn't store state directly. Instead:
 * 1. Commands produce EVENTS
 * 2. Events are APPLIED to update state
 * 3. State can be REBUILT by replaying all events
 * 
 * This gives you:
 * - Full audit trail (every change is an event)
 * - Time travel (replay to any point)
 * - Event-driven architecture (publish events to other services)
 */
public class Order {

    private UUID id;
    private String customerId;
    private Map<String, LineItem> items = new LinkedHashMap<>();
    private OrderStatus status;

    // Uncommitted events from the current transaction
    private final List<OrderEvent> uncommittedEvents = new ArrayList<>();

    // --- COMMANDS (produce events) ---

    public static Order create(String customerId) {
        Order order = new Order();
        order.apply(new OrderCreated(UUID.randomUUID(), customerId, LocalDateTime.now()));
        return order;
    }

    public void addItem(String productId, String name, int quantity, BigDecimal unitPrice) {
        if (status != OrderStatus.DRAFT) throw new IllegalStateException("Cannot modify non-draft order");
        apply(new ItemAdded(id, productId, name, quantity, unitPrice, LocalDateTime.now()));
    }

    public void removeItem(String productId) {
        if (status != OrderStatus.DRAFT) throw new IllegalStateException("Cannot modify non-draft order");
        if (!items.containsKey(productId)) throw new IllegalStateException("Item not in order");
        apply(new ItemRemoved(id, productId, LocalDateTime.now()));
    }

    public void submit() {
        if (status != OrderStatus.DRAFT) throw new IllegalStateException("Only draft orders can be submitted");
        if (items.isEmpty()) throw new IllegalStateException("Cannot submit empty order");
        apply(new OrderSubmitted(id, LocalDateTime.now()));
    }

    public void approve() {
        if (status != OrderStatus.SUBMITTED) throw new IllegalStateException("Only submitted orders can be approved");
        apply(new OrderApproved(id, LocalDateTime.now()));
    }

    public void complete() {
        if (status != OrderStatus.APPROVED) throw new IllegalStateException("Only approved orders can be completed");
        apply(new OrderCompleted(id, LocalDateTime.now()));
    }

    public void cancel(String reason) {
        if (status == OrderStatus.COMPLETED) throw new IllegalStateException("Cannot cancel completed order");
        apply(new OrderCancelled(id, reason, LocalDateTime.now()));
    }

    // --- EVENT APPLICATION (updates state from events) ---

    private void apply(OrderEvent event) {
        handle(event);
        uncommittedEvents.add(event);
    }

    /**
     * Apply a historical event (replaying from event store).
     * Does NOT add to uncommitted list.
     */
    public void replay(OrderEvent event) {
        handle(event);
    }

    private void handle(OrderEvent event) {
        switch (event) {
            case OrderCreated e -> {
                this.id = e.orderId();
                this.customerId = e.customerId();
                this.status = OrderStatus.DRAFT;
            }
            case ItemAdded e -> {
                items.put(e.productId(), new LineItem(e.productId(), e.name(), e.quantity(), e.unitPrice()));
            }
            case ItemRemoved e -> {
                items.remove(e.productId());
            }
            case OrderSubmitted e -> this.status = OrderStatus.SUBMITTED;
            case OrderApproved e -> this.status = OrderStatus.APPROVED;
            case OrderCompleted e -> this.status = OrderStatus.COMPLETED;
            case OrderCancelled e -> this.status = OrderStatus.CANCELLED;
        }
    }

    // --- QUERIES ---

    public BigDecimal calculateTotal() {
        return items.values().stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<OrderEvent> getUncommittedEvents() { return List.copyOf(uncommittedEvents); }
    public void clearUncommittedEvents() { uncommittedEvents.clear(); }

    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public Collection<LineItem> getItems() { return List.copyOf(items.values()); }

    public record LineItem(String productId, String name, int quantity, BigDecimal unitPrice) {}
    public enum OrderStatus { DRAFT, SUBMITTED, APPROVED, COMPLETED, CANCELLED }
}
