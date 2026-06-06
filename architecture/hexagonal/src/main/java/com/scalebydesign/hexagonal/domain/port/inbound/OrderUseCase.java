package com.scalebydesign.hexagonal.domain.port.inbound;

import com.scalebydesign.hexagonal.domain.model.Order;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * INBOUND PORT (Driving Port)
 * 
 * This interface defines what the outside world can do with our application.
 * It is implemented by the Application Service (OrderService).
 * It is called by Driving Adapters (e.g., REST Controller).
 * 
 * Key Hexagonal Principle:
 * - The port belongs to the DOMAIN, not the adapter.
 * - The outside world depends on this interface, not the other way around.
 */
public interface OrderUseCase {

    Order createOrder(String customerId);

    Order addItemToOrder(UUID orderId, String productId, String productName, int quantity, BigDecimal price);

    Order confirmOrder(UUID orderId);

    Order payOrder(UUID orderId);

    Order cancelOrder(UUID orderId);

    Order getOrder(UUID orderId);
}
