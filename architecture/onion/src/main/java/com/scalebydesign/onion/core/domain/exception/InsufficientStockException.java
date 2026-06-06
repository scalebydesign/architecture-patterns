package com.scalebydesign.onion.core.domain.exception;

public class InsufficientStockException extends RuntimeException {

    private final int availableStock;
    private final int requestedQuantity;

    public InsufficientStockException(int availableStock, int requestedQuantity) {
        super("Insufficient stock. Available: " + availableStock + ", Requested: " + requestedQuantity);
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    public int getAvailableStock() { return availableStock; }
    public int getRequestedQuantity() { return requestedQuantity; }
}
