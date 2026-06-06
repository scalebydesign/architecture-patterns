package com.scalebydesign.hexagonal.application;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.scalebydesign.hexagonal.domain.exception.OrderNotFoundException;
import com.scalebydesign.hexagonal.domain.exception.PaymentFailedException;
import com.scalebydesign.hexagonal.domain.model.Order;
import com.scalebydesign.hexagonal.domain.port.inbound.OrderUseCase;
import com.scalebydesign.hexagonal.domain.port.outbound.OrderRepository;
import com.scalebydesign.hexagonal.domain.port.outbound.PaymentGateway;

@Service
public class OrderService implements OrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public OrderService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    public Order createOrder(String customerId) {
        log.info("Creating order for customer: {}", customerId);
        Order order = new Order(customerId);
        Order saved = orderRepository.save(order);
        log.info("Order created: id={}", saved.getId());
        return saved;
    }

    @Override
    public Order addItemToOrder(UUID orderId, String productId, String productName, int quantity, BigDecimal price) {
        log.info("Adding item to order: orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        Order order = findOrderOrThrow(orderId);
        order.addItem(productId, productName, quantity, price);
        return orderRepository.save(order);
    }

    @Override
    public Order confirmOrder(UUID orderId) {
        log.info("Confirming order: id={}", orderId);
        Order order = findOrderOrThrow(orderId);
        order.confirm();
        Order saved = orderRepository.save(order);
        log.info("Order confirmed: id={}, total={}", orderId, saved.calculateTotal());
        return saved;
    }

    @Override
    public Order payOrder(UUID orderId) {
        log.info("Processing payment for order: id={}", orderId);
        Order order = findOrderOrThrow(orderId);

        boolean paymentSuccess = paymentGateway.processPayment(
                order.getCustomerId(),
                order.calculateTotal()
        );

        if (!paymentSuccess) {
            log.error("Payment failed for order: id={}", orderId);
            throw new PaymentFailedException(orderId.toString());
        }

        order.markPaid();
        Order saved = orderRepository.save(order);
        log.info("Order paid successfully: id={}", orderId);
        return saved;
    }

    @Override
    public Order cancelOrder(UUID orderId) {
        log.info("Cancelling order: id={}", orderId);
        Order order = findOrderOrThrow(orderId);
        order.cancel();
        Order saved = orderRepository.save(order);
        log.info("Order cancelled: id={}", orderId);
        return saved;
    }

    @Override
    public Order getOrder(UUID orderId) {
        log.debug("Fetching order: id={}", orderId);
        return findOrderOrThrow(orderId);
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
    }
}
