package com.scalebydesign.state.pattern.domain;

/**
 * DRAFT state — initial state. Can submit or cancel.
 */
public class DraftState implements OrderState {

    @Override
    public String getName() { return "DRAFT"; }

    @Override
    public OrderState submit(Order context) {
        System.out.println("Order " + context.getId() + ": DRAFT → SUBMITTED");
        return new SubmittedState();
    }

    @Override
    public OrderState approve(Order context) {
        return illegalTransition("approve");
    }

    @Override
    public OrderState complete(Order context) {
        return illegalTransition("complete");
    }

    @Override
    public OrderState cancel(Order context) {
        System.out.println("Order " + context.getId() + ": DRAFT → CANCELLED");
        return new CancelledState();
    }
}
