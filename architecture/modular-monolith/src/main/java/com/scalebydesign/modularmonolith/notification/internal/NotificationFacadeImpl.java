package com.scalebydesign.modularmonolith.notification.internal;

import com.scalebydesign.modularmonolith.notification.api.NotificationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * NOTIFICATION MODULE — Facade Implementation
 *
 * In production, this would integrate with email/SMS services.
 * Here it simply logs the notification.
 */
@Service
class NotificationFacadeImpl implements NotificationFacade {

    private static final Logger log = LoggerFactory.getLogger(NotificationFacadeImpl.class);

    @Override
    public void sendOrderConfirmation(String customerId, String orderId) {
        log.info("📧 Notification sent to customer {}: Your order {} has been placed!", customerId, orderId);
    }
}
