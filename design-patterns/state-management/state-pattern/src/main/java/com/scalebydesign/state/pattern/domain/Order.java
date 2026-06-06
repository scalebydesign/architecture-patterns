package com.scalebydesign.state.pattern.domain;

import java.util.UUID;

/**
 * ORDER — delegates all state-dependent behavior to the current state object.
 * 
 * The Order itself has NO if/switch on status.
 * Each state class decides what's allowed and returns the next state.
 */
public class Order {

    private UUID id;
    private String customerId;
    private OrderState state;

    public Order(String customerId) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.state = new DraftState();  // Initial state
    }

    // All transitions delegate to the current state
    public void submit() { this.state = state.submit(this); }
    public void approve() { this.state = state.approve(this); }
    public void complete() { this.state = state.complete(this); }
    public void cancel() { this.state = state.cancel(this); }

    // Getters
    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getStatus() { return state.getName(); }
    public OrderState getState() { return state; }
}
