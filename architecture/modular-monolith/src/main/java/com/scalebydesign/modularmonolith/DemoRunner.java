package com.scalebydesign.modularmonolith;

import com.scalebydesign.modularmonolith.inventory.api.InventoryFacade;
import com.scalebydesign.modularmonolith.order.api.OrderDto;
import com.scalebydesign.modularmonolith.order.api.OrderFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DEMO — Runs on startup to show modular communication in action.
 *
 * Demonstrates:
 * 1. Modules communicate via facades (synchronous) and events (asynchronous)
 * 2. Placing an order triggers inventory reservation AND notification
 * 3. No module reaches into another's internals
 */
@Component
public class DemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private final OrderFacade orderFacade;
    private final InventoryFacade inventoryFacade;

    public DemoRunner(OrderFacade orderFacade, InventoryFacade inventoryFacade) {
        this.orderFacade = orderFacade;
        this.inventoryFacade = inventoryFacade;
    }

    @Override
    public void run(String... args) {
        log.info("=== MODULAR MONOLITH DEMO ===\n");

        // Check initial stock
        log.info("📦 Initial stock for PROD-1: {}", inventoryFacade.getStock("PROD-1"));
        log.info("📦 Initial stock for PROD-2: {}", inventoryFacade.getStock("PROD-2"));

        // Place an order — this will:
        //   1. Create order (Order module)
        //   2. Reserve stock (Inventory module reacts via event)
        //   3. Send notification (Notification module reacts via event)
        log.info("\n--- Placing Order 1 ---");
        OrderDto order1 = orderFacade.placeOrder("customer-1", "PROD-1", 3);
        log.info("Order result: {} | total: ${} | status: {}", order1.orderId(), order1.totalPrice(), order1.status());

        log.info("\n--- Placing Order 2 ---");
        OrderDto order2 = orderFacade.placeOrder("customer-2", "PROD-2", 5);
        log.info("Order result: {} | total: ${} | status: {}", order2.orderId(), order2.totalPrice(), order2.status());

        // Check updated stock — reduced by event-driven reservation
        log.info("\n📦 Stock after orders:");
        log.info("  PROD-1: {} (was 50, reserved 3)", inventoryFacade.getStock("PROD-1"));
        log.info("  PROD-2: {} (was 100, reserved 5)", inventoryFacade.getStock("PROD-2"));

        log.info("\n=== DEMO COMPLETE ===");
        log.info("Modules communicated via events — no direct coupling!");
        log.info("Try the REST API: POST http://localhost:8095/api/modular/orders");
    }
}
