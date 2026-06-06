package com.scalebydesign.cqrs.command;

/**
 * COMMAND — represents an INTENT to change state.
 * Commands are imperative: "Create this order", "Add this item"
 */
public record CreateOrderCommand(String customerId) {}
