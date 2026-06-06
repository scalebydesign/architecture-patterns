package com.scalebydesign.hexagonal.domain.exception;

/**
 * Domain exception — thrown when an order cannot be found.
 * This is a domain concept, so it lives in the domain layer.
 */
public class OrderNotFoundException extends RuntimeException {

    private final String orderId;

    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
}
