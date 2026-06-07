package com.scalebydesign.modularmonolith.notification.api;

/**
 * NOTIFICATION MODULE — Public API (Facade)
 *
 * Exposes notification capabilities to other modules.
 */
public interface NotificationFacade {

    void sendOrderConfirmation(String customerId, String orderId);
}
