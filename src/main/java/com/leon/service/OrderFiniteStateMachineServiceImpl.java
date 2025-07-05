package com.leon.service;

import com.leon.model.OrderStateEvents;
import com.leon.model.OrderStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrderFiniteStateMachineServiceImpl implements OrderFiniteStateMachineService
{
    private static final Logger logger = LoggerFactory.getLogger(OrderFiniteStateMachineServiceImpl.class);
    @Autowired
    private StateMachine<OrderStates, OrderStateEvents> stateMachine;

    @Override
    public void sendEvent(String orderId, OrderStateEvents event)
    {
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(event).setHeader("orderId", orderId).build()))
            .subscribe(result -> logger.info("Event {} sent for order {} with result {}", event, orderId, result.getResultType()));
    }
}
