package com.scalebydesign.modularmonolith.inventory.internal;

import com.scalebydesign.modularmonolith.inventory.api.InventoryFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * INVENTORY MODULE — Facade Implementation
 *
 * Handles stock checks and reservations.
 * Reacts to OrderPlacedEvent to reserve stock.
 */
@Service
class InventoryFacadeImpl implements InventoryFacade {

    private static final Logger log = LoggerFactory.getLogger(InventoryFacadeImpl.class);

    private final StockRepository stockRepository;

    InventoryFacadeImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public int getStock(String productId) {
        return stockRepository.getQuantity(productId);
    }

    @Override
    public boolean isAvailable(String productId, int quantity) {
        return stockRepository.getQuantity(productId) >= quantity;
    }

    /**
     * Called when an order is placed (via EventBus subscription).
     * Reserves stock for the order.
     */
    void reserveStock(String productId, int quantity) {
        int current = stockRepository.getQuantity(productId);
        if (current >= quantity) {
            stockRepository.setQuantity(productId, current - quantity);
            log.info("✅ Stock reserved: {} x {} (remaining: {})", productId, quantity, current - quantity);
        } else {
            log.warn("❌ Insufficient stock for {}: requested={}, available={}", productId, quantity, current);
        }
    }
}
