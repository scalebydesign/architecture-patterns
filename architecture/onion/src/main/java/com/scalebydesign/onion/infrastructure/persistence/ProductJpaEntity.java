package com.scalebydesign.onion.infrastructure.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * OUTERMOST LAYER - Infrastructure
 * 
 * JPA entity is an infrastructure concern. It knows about the database
 * but the inner layers don't know it exists.
 */
@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    private UUID id;

    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String category;

    protected ProductJpaEntity() {}

    public ProductJpaEntity(UUID id, String name, String description, BigDecimal price, int stockQuantity, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
