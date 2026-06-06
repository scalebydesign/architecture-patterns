package com.scalebydesign.state.ssm.config;

import com.scalebydesign.state.ssm.domain.OrderEvent;
import com.scalebydesign.state.ssm.domain.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

/**
 * STATE MANAGEMENT: Spring State Machine
 * 
 * The state machine is defined DECLARATIVELY via configuration.
 * The framework handles state storage, transitions, guards, and actions.
 * 
 * Pros:
 * - Declarative configuration — states and transitions are data, not code
 * - Built-in persistence (can persist state to DB)
 * - Guards (conditional transitions), Actions (side effects on transition)
 * - Visual tooling support
 * - Battle-tested framework
 * 
 * Cons:
 * - Heavy framework dependency
 * - Overkill for simple state machines
 * - Learning curve (Spring State Machine API is complex)
 * - Couples domain to Spring
 * - Harder to test than plain Java approaches
 */
@Configuration
@EnableStateMachineFactory
public class OrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderStateMachineConfig.class);

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStatus, OrderEvent> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(new StateMachineListenerAdapter<>() {
                    @Override
                    public void stateChanged(State<OrderStatus, OrderEvent> from, State<OrderStatus, OrderEvent> to) {
                        log.info("State changed: {} → {}",
                                from != null ? from.getId() : "NONE",
                                to != null ? to.getId() : "NONE");
                    }
                });
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states
                .withStates()
                .initial(OrderStatus.DRAFT)
                .end(OrderStatus.COMPLETED)
                .end(OrderStatus.CANCELLED)
                .states(EnumSet.allOf(OrderStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions) throws Exception {
        transitions
                // DRAFT → SUBMITTED (on SUBMIT event)
                .withExternal()
                .source(OrderStatus.DRAFT).target(OrderStatus.SUBMITTED).event(OrderEvent.SUBMIT)
                .and()

                // SUBMITTED → APPROVED (on APPROVE event)
                .withExternal()
                .source(OrderStatus.SUBMITTED).target(OrderStatus.APPROVED).event(OrderEvent.APPROVE)
                .and()

                // APPROVED → COMPLETED (on COMPLETE event)
                .withExternal()
                .source(OrderStatus.APPROVED).target(OrderStatus.COMPLETED).event(OrderEvent.COMPLETE)
                .and()

                // Cancel from any non-terminal state
                .withExternal()
                .source(OrderStatus.DRAFT).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL)
                .and()
                .withExternal()
                .source(OrderStatus.SUBMITTED).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL)
                .and()
                .withExternal()
                .source(OrderStatus.APPROVED).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL);
    }
}
