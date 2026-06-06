package com.scalebydesign.hexagonal_onion.core.domain.exception;

public class InvalidCartOperationException extends RuntimeException {

    public InvalidCartOperationException(String message) {
        super(message);
    }
}
