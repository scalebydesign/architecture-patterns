package com.scalebydesign.richdomain.domain;

import java.math.BigDecimal;

/**
 * RICH DOMAIN - Value Object with behavior.
 * Even the line item protects its own invariants.
 */
public class LineItem {

    private final String productId;
    private final String name;
    private int quantity;
    private final BigDecimal unitPrice;

    public LineItem(String productId, String name, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.quantity += amount;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
