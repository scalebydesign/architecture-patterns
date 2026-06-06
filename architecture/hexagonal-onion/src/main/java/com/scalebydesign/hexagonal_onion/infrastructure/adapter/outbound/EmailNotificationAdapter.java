package com.scalebydesign.hexagonal_onion.infrastructure.adapter.outbound;

import com.scalebydesign.hexagonal_onion.application.port.outbound.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DRIVEN ADAPTER (Hexagonal) / OUTERMOST LAYER (Onion)
 * 
 * Implements the NotificationService outbound port.
 * In a real app, this would use SendGrid, SES, or another email provider.
 * 
 * Tomorrow you could swap this for SmsNotificationAdapter, PushNotificationAdapter,
 * or a composite — without touching ANY inner layer code.
 */
@Component
public class EmailNotificationAdapter implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    @Override
    public void sendOrderConfirmation(String email, String message) {
        // Simulate sending email
        log.info("Sending email to: {}", email);
        log.info("Message: {}", message);
        log.info("Email sent successfully!");
    }
}
