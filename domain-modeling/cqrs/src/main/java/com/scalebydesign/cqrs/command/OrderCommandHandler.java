package com.scalebydesign.cqrs.command;

import com.scalebydesign.cqrs.write.Order;
import com.scalebydesign.cqrs.write.OrderWriteRepository;
import com.scalebydesign.cqrs.read.OrderReadModel;
import com.scalebydesign.cqrs.read.OrderReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * COMMAND HANDLER — processes commands (writes)
 * 
 * In CQRS, the write side uses the RICH domain model.
 * After mutation, it syncs the read model (denormalized view).
 */
@Service
public class OrderCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandHandler.class);

    private final OrderWriteRepository writeRepository;
    private final OrderReadRepository readRepository;

    public OrderCommandHandler(OrderWriteRepository writeRepository, OrderReadRepository readRepository) {
        this.writeRepository = writeRepository;
        this.readRepository = readRepository;
    }

    public UUID handle(CreateOrderCommand command) {
        log.info("Handling CreateOrderCommand: customerId={}", command.customerId());
        Order order = new Order(command.customerId());
        writeRepository.save(order);
        syncReadModel(order);
        return order.getId();
    }

    public void handle(AddItemCommand command) {
        log.info("Handling AddItemCommand: orderId={}, productId={}", command.orderId(), command.productId());
        Order order = writeRepository.findById(command.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.addItem(command.productId(), command.name(), command.quantity(), command.unitPrice());
        writeRepository.save(order);
        syncReadModel(order);
    }

    public void handle(SubmitOrderCommand command) {
        log.info("Handling SubmitOrderCommand: orderId={}", command.orderId());
        Order order = writeRepository.findById(command.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.submit();
        writeRepository.save(order);
        syncReadModel(order);
    }

    /**
     * Sync the READ model after every write.
     * In real systems, this might be async via events.
     */
    private void syncReadModel(Order order) {
        OrderReadModel readModel = new OrderReadModel(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.calculateTotal(),
                order.getTotalItemCount(),
                order.getCreatedAt()
        );
        readRepository.save(readModel);
        log.debug("Read model synced for order: {}", order.getId());
    }
}
