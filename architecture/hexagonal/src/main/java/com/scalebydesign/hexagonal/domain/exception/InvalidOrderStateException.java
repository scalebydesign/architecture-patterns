package com.scalebydesign.hexagonal.domain.exception;

/**
 * Domain exception — thrown when an operation violates the order's current state.
 * Example: trying to add items to a confirmed order.
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
