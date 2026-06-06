package com.scalebydesign.hexagonal.domain.port.outbound;

import java.math.BigDecimal;

/**
 * OUTBOUND PORT (Driven Port)
 * 
 * Defines the contract for payment processing.
 * The domain doesn't know or care whether this is Stripe, PayPal, or a mock.
 * 
 * The adapter that implements this can be swapped without touching domain code.
 */
public interface PaymentGateway {

    boolean processPayment(String customerId, BigDecimal amount);
}
