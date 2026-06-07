package com.scalebydesign.modularmonolith.inventory.api;

/**
 * INVENTORY MODULE — Public API (Facade)
 *
 * Exposes stock queries. Other modules call this, never internal classes.
 */
public interface InventoryFacade {

    int getStock(String productId);

    boolean isAvailable(String productId, int quantity);
}
