package com.scalebydesign.hexagonal_onion.application.port.outbound;

/**
 * OUTBOUND PORT (Hexagonal: Driven Port)
 * 
 * Defines the contract for sending notifications.
 * Could be email, SMS, push notification — the domain doesn't care.
 * 
 * Combined Architecture:
 * - From HEXAGONAL: This is a driven port. Adapters implement it.
 * - From ONION: This interface lives in the application layer (inner).
 *   Implementation lives in infrastructure (outer).
 */
public interface NotificationService {

    void sendOrderConfirmation(String email, String message);
}
