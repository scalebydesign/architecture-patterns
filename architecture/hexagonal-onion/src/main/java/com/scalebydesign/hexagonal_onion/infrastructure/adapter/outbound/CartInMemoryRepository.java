package com.scalebydesign.hexagonal_onion.infrastructure.adapter.outbound;

import com.scalebydesign.hexagonal_onion.application.port.outbound.CartRepository;
import com.scalebydesign.hexagonal_onion.core.domain.model.ShoppingCart;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DRIVEN ADAPTER (Hexagonal) / OUTERMOST LAYER (Onion)
 * 
 * Combined Architecture:
 * - From HEXAGONAL: This is a Driven Adapter. It implements an outbound port.
 *   The application layer says "I need a CartRepository" (port).
 *   This adapter says "Here's an in-memory implementation."
 * - From ONION: The interface is defined in an inner layer (application).
 *   This implementation lives in the outermost layer (infrastructure).
 * 
 * Using in-memory storage here for simplicity. In production, swap with
 * a JPA/Redis/MongoDB adapter without touching ANY inner layer code.
 */
@Repository
public class CartInMemoryRepository implements CartRepository {

    private final Map<UUID, ShoppingCart> store = new ConcurrentHashMap<>();

    @Override
    public ShoppingCart save(ShoppingCart cart) {
        store.put(cart.getId(), cart);
        return cart;
    }

    @Override
    public Optional<ShoppingCart> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }
}
