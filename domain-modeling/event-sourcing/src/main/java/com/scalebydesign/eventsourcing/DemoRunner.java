package com.scalebydesign.eventsourcing;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.scalebydesign.eventsourcing.aggregate.Order;
import com.scalebydesign.eventsourcing.event.OrderEvent;
import com.scalebydesign.eventsourcing.service.OrderService;

/**
 * DEMO — Runs on application startup to demonstrate Event Sourcing in action.
 *
 * Shows:
 * 1. Events are appended as commands execute
 * 2. State is rebuilt by replaying events
 * 3. Full audit trail is preserved
 */
@Component
public class DemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);
    private final OrderService orderService;

    public DemoRunner(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        log.info("=== EVENT SOURCING DEMO ===\n");

        // 1. Create an order
        UUID orderId = orderService.createOrder("customer-42");
        log.info("✅ Order created: {}", orderId);

        // 2. Add items
        orderService.addItem(orderId, "PROD-1", "Mechanical Keyboard", 1, new BigDecimal("129.99"));
        orderService.addItem(orderId, "PROD-2", "USB-C Cable", 2, new BigDecimal("12.50"));
        orderService.addItem(orderId, "PROD-3", "Monitor Stand", 1, new BigDecimal("49.99"));
        log.info("✅ Items added");

        // 3. Submit the order
        orderService.submitOrder(orderId);
        log.info("✅ Order submitted");

        // 4. Approve the order
        orderService.approveOrder(orderId);
        log.info("✅ Order approved");

        // 5. Complete the order
        orderService.completeOrder(orderId);
        log.info("✅ Order completed");

        // --- Now demonstrate the key benefit: full event history ---
        log.info("\n=== EVENT HISTORY (source of truth) ===");
        List<OrderEvent> history = orderService.getOrderHistory(orderId);
        for (int i = 0; i < history.size(); i++) {
            log.info("  Event {}: {}", i + 1, formatEvent(history.get(i)));
        }

        // --- Demonstrate state rebuilt from events ---
        log.info("\n=== CURRENT STATE (rebuilt from {} events) ===", history.size());
        Order order = orderService.getOrder(orderId);
        log.info("  Order ID:    {}", order.getId());
        log.info("  Customer:    {}", order.getCustomerId());
        log.info("  Status:      {}", order.getStatus());
        log.info("  Total:       ${}", order.calculateTotal());
        log.info("  Items:");
        order.getItems().forEach(item ->
                log.info("    - {} x{} @ ${}", item.name(), item.quantity(), item.unitPrice())
        );

        log.info("\n=== DEMO COMPLETE ===");
    }

    private String formatEvent(OrderEvent event) {
        return switch (event) {
            case OrderEvent.OrderCreated e -> "OrderCreated(customer=%s)".formatted(e.customerId());
            case OrderEvent.ItemAdded e -> "ItemAdded(product=%s, qty=%d, price=$%s)".formatted(e.name(), e.quantity(), e.unitPrice());
            case OrderEvent.ItemRemoved e -> "ItemRemoved(product=%s)".formatted(e.productId());
            case OrderEvent.OrderSubmitted e -> "OrderSubmitted";
            case OrderEvent.OrderApproved e -> "OrderApproved";
            case OrderEvent.OrderCompleted e -> "OrderCompleted";
            case OrderEvent.OrderCancelled e -> "OrderCancelled(reason=%s)".formatted(e.reason());
        };
    }
}
