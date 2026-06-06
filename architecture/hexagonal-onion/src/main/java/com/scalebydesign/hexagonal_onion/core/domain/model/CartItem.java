package com.scalebydesign.hexagonal_onion.core.domain.model;

import com.scalebydesign.hexagonal_onion.core.domain.exception.InvalidCartOperationException;

import java.math.BigDecimal;

/**
 * CORE DOMAIN - Value Object
 * 
 * Innermost layer of the Onion. No dependencies on anything.
 */
public class CartItem {

    private final String productId;
    private final String productName;
    private int quantity;
    private final BigDecimal unitPrice;

    public CartItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) throw new InvalidCartOperationException("Quantity must be positive");
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) throw new InvalidCartOperationException("Price must be positive");
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) throw new InvalidCartOperationException("Amount must be positive");
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) throw new InvalidCartOperationException("Amount must be positive");
        if (amount > this.quantity) throw new InvalidCartOperationException("Cannot decrease below zero");
        this.quantity -= amount;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
