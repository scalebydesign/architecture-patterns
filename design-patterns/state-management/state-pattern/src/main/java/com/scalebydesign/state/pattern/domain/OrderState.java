package com.scalebydesign.state.pattern.domain;

/**
 * STATE MANAGEMENT: GoF State Pattern
 * 
 * Each state is a CLASS that defines what actions are allowed in that state.
 * The Order delegates all operations to its current state object.
 * 
 * Pros:
 * - Open/Closed Principle — add new states without modifying existing ones
 * - Each state class handles its own behavior (no giant switch/if chains)
 * - States can have entry/exit actions, different calculations
 * - Impossible to forget handling — interface forces all methods
 * 
 * Cons:
 * - Class explosion (one class per state)
 * - Harder to see the full state machine at a glance
 * - Over-engineered for simple 4-5 state machines
 * - Persistence is trickier (need to serialize/deserialize state type)
 */
public interface OrderState {

    String getName();

    OrderState submit(Order context);

    OrderState approve(Order context);

    OrderState complete(Order context);

    OrderState cancel(Order context);

    /**
     * Default behavior for illegal transitions.
     * Subclasses override only the transitions they allow.
     */
    default OrderState illegalTransition(String action) {
        throw new IllegalStateException(
                String.format("Cannot '%s' in state '%s'", action, getName()));
    }
}
