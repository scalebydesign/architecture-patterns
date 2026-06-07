package com.scalebydesign.modularmonolith.inventory.internal;

import com.scalebydesign.modularmonolith.order.api.OrderPlacedEvent;
import com.scalebydesign.modularmonolith.shared.EventBus;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * INVENTORY MODULE — Event Handler
 *
 * Subscribes to events from other modules.
 * This is how the inventory module REACTS to order placement
 * without the order module knowing about inventory at all.
 *
 * Key principle: The Order module publishes events.
 * The Inventory module subscribes. Neither depends on the other's internals.
 */
@Component
class InventoryEventHandler {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventHandler.class);

    private final InventoryFacadeImpl inventoryFacade;
    private final EventBus eventBus;

    InventoryEventHandler(InventoryFacadeImpl inventoryFacade, EventBus eventBus) {
        this.inventoryFacade = inventoryFacade;
        this.eventBus = eventBus;
    }

    @PostConstruct
    void subscribeToEvents() {
        eventBus.subscribe(OrderPlacedEvent.class, this::onOrderPlaced);
        log.debug("Inventory module subscribed to OrderPlacedEvent");
    }

    private void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Inventory reacting to OrderPlacedEvent: orderId={}", event.orderId());
        inventoryFacade.reserveStock(event.productId(), event.quantity());
    }
}
