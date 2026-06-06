package com.scalebydesign.hexagonal.domain.port.outbound;

import com.scalebydesign.hexagonal.domain.model.Order;

import java.util.Optional;
import java.util.UUID;

/**
 * OUTBOUND PORT (Driven Port)
 * 
 * This interface defines what the domain NEEDS from the outside world.
 * It is defined in the domain but IMPLEMENTED by an infrastructure adapter.
 * 
 * Key Hexagonal Principle:
 * - The domain defines the contract (this interface).
 * - The infrastructure provides the implementation (adapter).
 * - This is the Dependency Inversion Principle in action.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);
}
