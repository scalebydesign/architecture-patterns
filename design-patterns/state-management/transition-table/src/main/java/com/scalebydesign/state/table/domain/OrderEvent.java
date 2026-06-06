package com.scalebydesign.state.table.domain;

/**
 * Events (triggers) that cause state transitions.
 * The transition table maps (CurrentState, Event) → NextState.
 */
public enum OrderEvent {
    SUBMIT,
    APPROVE,
    COMPLETE,
    CANCEL
}
