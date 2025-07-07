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

import static com.leon.model.OrderStates.NEW_ORDER;

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

    public void applyActionEvent(Order order)
    {
        log.info("Order received: {}", order.getOrderId());
        OrderStates currentState = order.getState();
        Optional<OrderStates> nextStateOpt = OrderStateMachine.getNextState(currentState, order.getActionEvent());

        if (nextStateOpt.isPresent())
        {
            OrderStates newState = nextStateOpt.get();
            order.setState(newState);
            orderService.saveOrder(order);
            ampsMessageOutboundProcessor.sendOrder(order);
            log.info("Order {} transitioned from {} to {} due to event {}", order.getOrderId(), currentState, newState, order.getActionEvent());
        }
        else
            log.warn("No valid transition for order {} from state {} with event {}", order.getOrderId(), currentState, order.getActionEvent());
    }

    private void processOrder(Order order)
    {
        if(order.getState() == NEW_ORDER && order.getActionEvent() == OrderStateEvents.SUBMIT_TO_OMS)
        {
            applyActionEvent(order);
            applyActionEvent(order);
        }
        else
            applyActionEvent(order);
    }
} 