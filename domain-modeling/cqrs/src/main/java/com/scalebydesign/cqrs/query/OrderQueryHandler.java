package com.scalebydesign.cqrs.query;

import com.scalebydesign.cqrs.read.OrderReadModel;
import com.scalebydesign.cqrs.read.OrderReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * QUERY HANDLER — handles reads from the READ model.
 * Fast, simple, no business logic. Just data retrieval.
 */
@Service
public class OrderQueryHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderQueryHandler.class);

    private final OrderReadRepository readRepository;

    public OrderQueryHandler(OrderReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    public OrderReadModel getOrder(UUID id) {
        log.debug("Query: getOrder id={}", id);
        return readRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public List<OrderReadModel> getAllOrders() {
        log.debug("Query: getAllOrders");
        return readRepository.findAll();
    }

    public List<OrderReadModel> getOrdersByCustomer(String customerId) {
        log.debug("Query: getOrdersByCustomer customerId={}", customerId);
        return readRepository.findByCustomerId(customerId);
    }

    public List<OrderReadModel> getOrdersByStatus(String status) {
        log.debug("Query: getOrdersByStatus status={}", status);
        return readRepository.findByStatus(status);
    }
}
