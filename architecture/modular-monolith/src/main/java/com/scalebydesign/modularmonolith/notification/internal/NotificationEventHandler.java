package com.scalebydesign.modularmonolith.notification.internal;

import com.scalebydesign.modularmonolith.order.api.OrderPlacedEvent;
import com.scalebydesign.modularmonolith.shared.EventBus;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * NOTIFICATION MODULE — Event Handler
 *
 * Reacts to OrderPlacedEvent by sending a confirmation.
 * The notification module has ZERO knowledge of Order internals.
 * It only knows about the published event contract.
 */
@Component
class NotificationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

    private final NotificationFacadeImpl notificationFacade;
    private final EventBus eventBus;

    NotificationEventHandler(NotificationFacadeImpl notificationFacade, EventBus eventBus) {
        this.notificationFacade = notificationFacade;
        this.eventBus = eventBus;
    }

    @PostConstruct
    void subscribeToEvents() {
        eventBus.subscribe(OrderPlacedEvent.class, this::onOrderPlaced);
        log.debug("Notification module subscribed to OrderPlacedEvent");
    }

    private void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Notification reacting to OrderPlacedEvent: orderId={}", event.orderId());
        notificationFacade.sendOrderConfirmation(event.customerId(), event.orderId().toString());
    }
}
