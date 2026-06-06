package com.scalebydesign.hexagonal.adapter.outbound;

import com.scalebydesign.hexagonal.domain.port.outbound.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * OUTBOUND ADAPTER (Driven Adapter)
 * 
 * Implements the PaymentGateway port.
 * In a real app, this would call the Stripe API.
 * Here it simulates a payment for demonstration.
 * 
 * Key Hexagonal Principle:
 * - Domain says "I need a PaymentGateway" (port).
 * - Infrastructure says "Here's Stripe" (adapter).
 * - Tomorrow you can swap this for PayPalPaymentAdapter without touching domain code.
 */
@Component
public class StripePaymentAdapter implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentAdapter.class);

    @Override
    public boolean processPayment(String customerId, BigDecimal amount) {
        // Simulate payment processing
        log.info("Processing payment via Stripe for customer: {}, amount: {}", customerId, amount);

        // Simulate: payments under $10,000 always succeed
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            log.warn("Payment declined: amount exceeds limit");
            return false;
        }

        log.info("Payment successful!");
        return true;
    }
}
