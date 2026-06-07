package com.scalebydesign.modularmonolith.inventory.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * INVENTORY MODULE — Internal Repository
 *
 * Package-private, invisible to other modules.
 * Pre-loaded with sample stock data.
 */
@Repository
class StockRepository {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of(
            "PROD-1", 50,
            "PROD-2", 100,
            "PROD-3", 25
    ));

    int getQuantity(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    void setQuantity(String productId, int quantity) {
        stock.put(productId, quantity);
    }
}
