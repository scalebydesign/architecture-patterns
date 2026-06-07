package com.scalebydesign.modularmonolith.order.internal;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ORDER MODULE — Internal Entity
 *
 * This class is NOT accessible from outside the order module.
 * Other modules interact only via OrderFacade and OrderDto.
 */
class Order {

    private UUID id;
    private String customerId;
    private String productId;
    private int quantity;
    private BigDecimal unitPrice;
    private OrderStatus status;

    Order(String customerId, String productId, int quantity, BigDecimal unitPrice) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = OrderStatus.PLACED;
    }

    UUID getId() { return id; }
    String getCustomerId() { return customerId; }
    String getProductId() { return productId; }
    int getQuantity() { return quantity; }
    BigDecimal getTotalPrice() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    OrderStatus getStatus() { return status; }

    void confirm() { this.status = OrderStatus.CONFIRMED; }
    void reject() { this.status = OrderStatus.REJECTED; }

    enum OrderStatus { PLACED, CONFIRMED, REJECTED }
}
