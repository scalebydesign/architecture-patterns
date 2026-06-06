package com.scalebydesign.hexagonal_onion.core.domain.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot checkout an empty cart");
    }
}
