package com.scalebydesign.modularmonolith.order.api;

import java.util.UUID;

/**
 * ORDER MODULE — Public API (Facade)
 *
 * This is the ONLY class other modules are allowed to call.
 * It hides internal domain objects, repositories, and services.
 *
 * Rule: No module may access another module's 'internal' package.
 */
public interface OrderFacade {

    OrderDto placeOrder(String customerId, String productId, int quantity);

    OrderDto getOrder(UUID orderId);
}
