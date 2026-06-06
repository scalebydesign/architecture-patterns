package com.scalebydesign.onion.core.domain;

import com.scalebydesign.onion.core.domain.exception.InsufficientStockException;
import com.scalebydesign.onion.core.domain.exception.InvalidProductException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * INNERMOST LAYER - Domain Entity
 * 
 * In Onion Architecture, this is the CENTER of the onion.
 * It has ZERO dependencies on any other layer.
 * 
 * All other layers depend on this layer, but this layer depends on NOTHING.
 * 
 * Onion Architecture Layers (inside → outside):
 * 1. Domain Model (THIS) — entities, value objects, domain logic
 * 2. Domain Services — operations that span multiple entities
 * 3. Application Services — orchestration, use cases
 * 4. Infrastructure — frameworks, DB, external services
 */
public class Product {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String category;

    public Product(String name, String description, BigDecimal price, int stockQuantity, String category) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        validate();
    }

    // Domain business rules
    private void validate() {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Product name cannot be empty");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductException("Price must be positive");
        }
        if (stockQuantity < 0) {
            throw new InvalidProductException("Stock cannot be negative");
        }
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public void reduceStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new InsufficientStockException(stockQuantity, quantity);
        }
        this.stockQuantity -= quantity;
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidProductException("Restock quantity must be positive");
        }
        this.stockQuantity += quantity;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductException("Price must be positive");
        }
        this.price = newPrice;
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public String getCategory() { return category; }

    // For persistence reconstruction
    public void setId(UUID id) { this.id = id; }
}
