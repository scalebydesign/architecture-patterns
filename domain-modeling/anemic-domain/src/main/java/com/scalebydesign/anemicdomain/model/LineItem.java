package com.scalebydesign.anemicdomain.model;

import java.math.BigDecimal;

/**
 * ANEMIC - LineItem is also just data. No behavior.
 */
public class LineItem {

    private String productId;
    private String name;
    private int quantity;
    private BigDecimal unitPrice;

    public LineItem() {}

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
