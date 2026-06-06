package com.scalebydesign.hexagonal.domain.exception;

/**
 * Domain exception — thrown when payment processing fails.
 */
public class PaymentFailedException extends RuntimeException {

    private final String orderId;

    public PaymentFailedException(String orderId) {
        super("Payment failed for order: " + orderId);
        this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
}
