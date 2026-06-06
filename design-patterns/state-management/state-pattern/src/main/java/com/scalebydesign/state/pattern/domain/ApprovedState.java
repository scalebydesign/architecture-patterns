package com.scalebydesign.state.pattern.domain;

/**
 * APPROVED state — ready for fulfillment. Can complete or cancel.
 */
public class ApprovedState implements OrderState {

    @Override
    public String getName() { return "APPROVED"; }

    @Override
    public OrderState submit(Order context) {
        return illegalTransition("submit");
    }

    @Override
    public OrderState approve(Order context) {
        return illegalTransition("approve");
    }

    @Override
    public OrderState complete(Order context) {
        System.out.println("Order " + context.getId() + ": APPROVED → COMPLETED");
        return new CompletedState();
    }

    @Override
    public OrderState cancel(Order context) {
        System.out.println("Order " + context.getId() + ": APPROVED → CANCELLED");
        return new CancelledState();
    }
}
