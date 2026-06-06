package com.scalebydesign.hexagonal_onion.application.port.inbound;

import com.scalebydesign.hexagonal_onion.core.domain.model.ShoppingCart;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * INBOUND PORT (Hexagonal: Driving Port)
 * 
 * Combined Architecture:
 * - From HEXAGONAL: This defines what the outside world can do with our application.
 *   Driving adapters (REST, CLI, GraphQL) depend on this interface.
 * - From ONION: This lives in the Application layer (one ring out from domain core).
 *   It can depend on the core domain but not on infrastructure.
 */
public interface CartUseCase {

    ShoppingCart createCart(String customerId, String email, String tier);

    ShoppingCart addItemToCart(UUID cartId, String productId, String productName, int quantity, BigDecimal price);

    ShoppingCart removeItemFromCart(UUID cartId, String productId);

    ShoppingCart updateItemQuantity(UUID cartId, String productId, int quantity);

    ShoppingCart getCart(UUID cartId);

    BigDecimal checkout(UUID cartId);

    void clearCart(UUID cartId);
}
