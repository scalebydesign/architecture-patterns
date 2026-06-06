package com.scalebydesign.hexagonal_onion.application.port.outbound;

import com.scalebydesign.hexagonal_onion.core.domain.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

/**
 * OUTBOUND PORT (Hexagonal: Driven Port)
 * 
 * Combined Architecture:
 * - From HEXAGONAL: The domain/application layer defines this contract.
 *   Infrastructure provides the implementation.
 * - From ONION: This interface sits in the application layer, pointing inward.
 *   The implementation (adapter) sits in the outermost infrastructure layer.
 */
public interface CartRepository {

    ShoppingCart save(ShoppingCart cart);

    Optional<ShoppingCart> findById(UUID id);

    void deleteById(UUID id);
}
