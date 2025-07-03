package com.leon.service;

import com.leon.model.OrderStateEvents;
import com.leon.model.OrderStates;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class OrderFiniteStateMachineService extends EnumStateMachineConfigurerAdapter<OrderStates, OrderStateEvents> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderStates, OrderStateEvents> states) throws Exception {
        states.withStates().initial(OrderStates.NEW).states(EnumSet.allOf(OrderStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStates, OrderStateEvents> transitions) throws Exception {
        transitions.withExternal()
            .source(OrderStates.NEW).target(OrderStates.NEW_ACK)
            .event(OrderStateEvents.SUBMIT_TO_DESK);
    }
}
