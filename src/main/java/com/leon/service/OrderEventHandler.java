package com.leon.service;

import com.leon.messaging.AmpsMessageOutboundProcessor;
import com.leon.model.OrderStateEvents;
import com.leon.model.OrderStates;
import com.lmax.disruptor.EventHandler;
import com.leon.model.Order;
import com.leon.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderEventHandler implements EventHandler<OrderEvent>
{
    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);
    @Autowired
    private OrderService orderService;
    @Autowired
    private AmpsMessageOutboundProcessor ampsMessageOutboundProcessor;
    @Autowired
    private OrderStateMachine orderStateMachine;

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch)
    {
        try
        {
            processOrder(event.getOrder());
        }
        catch(Exception e)
        {
            log.error("Error processing order event: {}", event, e);
        }
    }

    public void applyEvent(Order order, OrderStateEvents event)
    {
        log.info("Order received: {}", order.getOrderId());
        OrderStates currentState = order.getState();
        Optional<OrderStates> nextStateOpt = OrderStateMachine.getNextState(currentState, event);

        if (nextStateOpt.isPresent())
        {
            OrderStates newState = nextStateOpt.get();
            order.setState(newState);
            orderService.saveOrder(order);
            ampsMessageOutboundProcessor.sendOrder(order);
            log.info("Order {} transitioned from {} to {} due to event {}", order.getOrderId(), currentState, newState, event);
        }
        else
        {
            log.warn("No valid transition for order {} from state {} with event {}", order.getOrderId(), currentState, event);
        }
    }

    private void processOrder(Order order)
    {
        switch(order.getState())
        {
            case NEW_ORDER:
                applyEvent(order, OrderStateEvents.SUBMIT_TO_DESK);
                break;
            case PENDING_NEW:
                applyEvent(order, OrderStateEvents.OMS_ACCEPT);
                break;
            default:
                log.warn("Unknown order state not handled: {}", order.getState());
                break;
        }
    }
} 