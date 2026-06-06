package com.scalebydesign.state.pattern.domain;

/**
 * SUBMITTED state — awaiting approval. Can approve or cancel.
 */
public class SubmittedState implements OrderState {

    @Override
    public String getName() { return "SUBMITTED"; }

    @Override
    public OrderState submit(Order context) {
        return illegalTransition("submit");
    }

    @Override
    public OrderState approve(Order context) {
        System.out.println("Order " + context.getId() + ": SUBMITTED → APPROVED");
        return new ApprovedState();
    }

    @Override
    public OrderState complete(Order context) {
        return illegalTransition("complete");
    }

    @Override
    public OrderState cancel(Order context) {
        System.out.println("Order " + context.getId() + ": SUBMITTED → CANCELLED");
        return new CancelledState();
    }
}
