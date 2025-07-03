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
            .source(OrderStates.NEW).target(OrderStates.PENDING_NEW).event(OrderStateEvents.SUBMIT_TO_DESK).and()
                .withExternal().source(OrderStates.PENDING_NEW).target(OrderStates.NEW_ACK).event(OrderStateEvents.OMS_ACCEPT).and()
                .withExternal().source(OrderStates.PENDING_NEW).target(OrderStates.REJECTED_BY_OMS).event(OrderStateEvents.OMS_REJECT).and()
                .withExternal().source(OrderStates.NEW_ACK).target(OrderStates.ACCEPTED_BY_DESK).event(OrderStateEvents.DESK_APPROVE).and()
                .withExternal().source(OrderStates.NEW_ACK).target(OrderStates.REJECTED_BY_DESK).event(OrderStateEvents.DESK_REJECT).and()
                .withExternal().source(OrderStates.ACCEPTED_BY_DESK).target(OrderStates.SENT_TO_EXCHANGE).event(OrderStateEvents.SUBMIT_TO_EXCHANGE).and()
                .withExternal().source(OrderStates.SENT_TO_EXCHANGE).target(OrderStates.ACKNOWLEDGED_BY_EXCHANGE).event(OrderStateEvents.EXCHANGE_ACKNOWLEDGE).and()
                .withExternal().source(OrderStates.SENT_TO_EXCHANGE).target(OrderStates.REJECTED_BY_EXCHANGE).event(OrderStateEvents.EXCHANGE_REJECT);
    }
}
