package com.scalebydesign.anemicdomain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ANEMIC DOMAIN MODEL
 * 
 * The entity is a PURE DATA BAG — only getters and setters.
 * It has NO behavior, NO validation, NO business rules.
 * 
 * ALL logic lives in the Service layer.
 * 
 * Key characteristics:
 * - Entity cannot protect itself
 * - Anyone can call setStatus() and put it in an invalid state
 * - Business rules are in OrderService, not here
 * - Simple to understand, but violates encapsulation
 */
public class Order {

    private UUID id;
    private String customerId;
    private List<LineItem> items;
    private String status;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Order() {
        this.id = UUID.randomUUID();
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    // ONLY getters and setters — NO business logic

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<LineItem> getItems() { return items; }
    public void setItems(List<LineItem> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
