package com.scalebydesign.hexagonal.domain.model;

import java.math.BigDecimal;

/**
 * Value Object - OrderItem
 * 
 * Immutable value object representing a line item in an order.
 */
public class OrderItem {

    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal price;

    public OrderItem(String productId, String productName, int quantity, BigDecimal price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
}
