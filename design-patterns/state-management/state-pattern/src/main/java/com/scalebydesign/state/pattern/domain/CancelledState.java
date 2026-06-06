package com.scalebydesign.state.pattern.domain;

/**
 * CANCELLED state — terminal. No transitions allowed.
 */
public class CancelledState implements OrderState {

    @Override
    public String getName() { return "CANCELLED"; }

    @Override
    public OrderState submit(Order context) { return illegalTransition("submit"); }

    @Override
    public OrderState approve(Order context) { return illegalTransition("approve"); }

    @Override
    public OrderState complete(Order context) { return illegalTransition("complete"); }

    @Override
    public OrderState cancel(Order context) { return illegalTransition("cancel"); }
}
