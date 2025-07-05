package com.leon.service;

import com.leon.model.OrderStateEvents;
import com.leon.model.OrderStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class OrderFiniteStateMachineConfiguration extends StateMachineConfigurerAdapter<OrderStates, OrderStateEvents>
{
    private static final Logger logger = LoggerFactory.getLogger(DisruptorServiceImpl.class);
    @Autowired
    private OrderService orderService;
    @Autowired
    private StateMachineModelFactory modelFactory;
    @Override
    public void configure(StateMachineStateConfigurer<OrderStates, OrderStateEvents> states) throws Exception {
        states.withStates().initial(OrderStates.NEW).states(EnumSet.allOf(OrderStates.class));
    }

    @Override
    public void configure(StateMachineModelConfigurer<OrderStates, OrderStateEvents> model) throws Exception {
        model.withModel().factory(modelFactory);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStates, OrderStateEvents> config) throws Exception {
        config.withConfiguration().autoStartup(true);
    }

    @Bean
    public Action<OrderStates, OrderStateEvents> updateOrderStateAction() {
        return context -> {
            String orderId = (String) context.getExtendedState().getVariables().get("orderId");
            OrderStates newState = context.getTarget().getId();

            logger.info("Order {} transitioned to {} because of event {}", orderId, newState, context.getEvent());

            orderService.getOrderById(orderId).ifPresent(order -> {
                order.setState(newState);
                orderService.saveOrder(order);
            });
        };
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStates, OrderStateEvents> transitions) throws Exception {
        transitions.withExternal()
            .source(OrderStates.NEW).target(OrderStates.PENDING_NEW).event(OrderStateEvents.SUBMIT_TO_DESK).action(updateOrderStateAction()).and()
                .withExternal().source(OrderStates.PENDING_NEW).target(OrderStates.NEW_ACK).event(OrderStateEvents.OMS_ACCEPT).action(updateOrderStateAction()).and()
                .withExternal().source(OrderStates.PENDING_NEW).target(OrderStates.REJECTED_BY_OMS).event(OrderStateEvents.OMS_REJECT).action(updateOrderStateAction()).and()
                .withExternal().source(OrderStates.NEW_ACK).target(OrderStates.ACCEPTED_BY_DESK).event(OrderStateEvents.DESK_APPROVE).action(updateOrderStateAction()).and()
                .withExternal().source(OrderStates.NEW_ACK).target(OrderStates.REJECTED_BY_DESK).event(OrderStateEvents.DESK_REJECT).action(updateOrderStateAction()).and()
                .withExternal().source(OrderStates.ACCEPTED_BY_DESK).target(OrderStates.SENT_TO_EXCHANGE).event(OrderStateEvents.SUBMIT_TO_EXCHANGE).action(updateOrderStateAction()).and()
                .withExternal().source(OrderStates.SENT_TO_EXCHANGE).target(OrderStates.ACKNOWLEDGED_BY_EXCHANGE).event(OrderStateEvents.EXCHANGE_ACKNOWLEDGE).action(updateOrderStateAction()).and()
                .withExternal().source(OrderStates.SENT_TO_EXCHANGE).target(OrderStates.REJECTED_BY_EXCHANGE).event(OrderStateEvents.EXCHANGE_REJECT).action(updateOrderStateAction());
    }


}
