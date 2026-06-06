package com.scalebydesign.state.ssm.service;

import com.scalebydesign.state.ssm.domain.OrderEvent;
import com.scalebydesign.state.ssm.domain.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that manages Order state machines.
 * Each order gets its own StateMachine instance.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final StateMachineFactory<OrderStatus, OrderEvent> factory;
    private final Map<UUID, StateMachine<OrderStatus, OrderEvent>> machines = new ConcurrentHashMap<>();

    public OrderService(StateMachineFactory<OrderStatus, OrderEvent> factory) {
        this.factory = factory;
    }

    public UUID createOrder() {
        UUID orderId = UUID.randomUUID();
        StateMachine<OrderStatus, OrderEvent> sm = factory.getStateMachine(orderId.toString());
        sm.startReactively().block();
        machines.put(orderId, sm);
        log.info("Order created: id={}, state={}", orderId, sm.getState().getId());
        return orderId;
    }

    public OrderStatus sendEvent(UUID orderId, OrderEvent event) {
        StateMachine<OrderStatus, OrderEvent> sm = machines.get(orderId);
        if (sm == null) throw new RuntimeException("Order not found: " + orderId);

        log.info("Sending event '{}' to order: {}", event, orderId);
        boolean accepted = sm.sendEvent(MessageBuilder.withPayload(event).build());

        OrderStatus currentState = sm.getState().getId();
        if (!accepted) {
            log.warn("Event '{}' rejected in state '{}'", event, currentState);
            throw new IllegalStateException(
                    String.format("Event '%s' not accepted in state '%s'", event, currentState));
        }

        log.info("Order {} is now in state: {}", orderId, currentState);
        return currentState;
    }

    public OrderStatus getStatus(UUID orderId) {
        StateMachine<OrderStatus, OrderEvent> sm = machines.get(orderId);
        if (sm == null) throw new RuntimeException("Order not found: " + orderId);
        return sm.getState().getId();
    }
}
