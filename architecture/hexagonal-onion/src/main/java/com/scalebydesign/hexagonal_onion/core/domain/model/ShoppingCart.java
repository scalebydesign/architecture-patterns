package com.scalebydesign.hexagonal_onion.core.domain.model;

import com.scalebydesign.hexagonal_onion.core.domain.exception.InvalidCartOperationException;

import java.math.BigDecimal;
import java.util.*;

/**
 * CORE DOMAIN - Aggregate Root (Onion: innermost layer)
 * 
 * Combined Architecture:
 * - From ONION: This sits at the center. Zero dependencies on outer layers.
 * - From HEXAGONAL: The domain defines what it needs via ports (interfaces in application layer).
 * 
 * All business rules for the shopping cart live HERE.
 */
public class ShoppingCart {

    private UUID id;
    private final Customer customer;
    private final Map<String, CartItem> items;  // productId -> CartItem

    public ShoppingCart(Customer customer) {
        this.id = UUID.randomUUID();
        this.customer = customer;
        this.items = new LinkedHashMap<>();
    }

    // --- Business Rules ---

    public void addItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        if (items.containsKey(productId)) {
            items.get(productId).increaseQuantity(quantity);
        } else {
            items.put(productId, new CartItem(productId, productName, quantity, unitPrice));
        }
    }

    public void removeItem(String productId) {
        if (!items.containsKey(productId)) {
            throw new InvalidCartOperationException("Item not in cart: " + productId);
        }
        items.remove(productId);
    }

    public void updateItemQuantity(String productId, int newQuantity) {
        CartItem item = items.get(productId);
        if (item == null) throw new InvalidCartOperationException("Item not in cart: " + productId);

        if (newQuantity <= 0) {
            items.remove(productId);
        } else {
            int diff = newQuantity - item.getQuantity();
            if (diff > 0) item.increaseQuantity(diff);
            else if (diff < 0) item.decreaseQuantity(-diff);
        }
    }

    public BigDecimal calculateSubtotal() {
        return items.values().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateShipping() {
        if (customer.isEligibleForFreeShipping()) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = calculateSubtotal();
        // Free shipping for orders over $50
        if (subtotal.compareTo(new BigDecimal("50")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("5.99");
    }

    public BigDecimal calculateTotal() {
        return calculateSubtotal().add(calculateShipping());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public void clear() {
        items.clear();
    }

    // Getters
    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public List<CartItem> getItems() { return List.copyOf(items.values()); }

    // For persistence
    public void setId(UUID id) { this.id = id; }
}
