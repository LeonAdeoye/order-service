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

import static com.leon.model.OrderStates.*;

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
    @Autowired
    private OrderAggregationService orderAggregationService;

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

    public void applyActionEvent(Order order, OrderStateEvents actionEvent)
    {
        log.info("Order received: {}", order.getOrderId());
        OrderStates currentState = order.getState();
        Optional<OrderStates> nextStateOpt = OrderStateMachine.getNextState(currentState, actionEvent);

        if (nextStateOpt.isPresent())
        {
            OrderStates newState = nextStateOpt.get();
            order.setState(newState);
            orderService.saveOrder(order);
            if(order.getState().equals(OrderStates.PENDING_EXCH))
                ampsMessageOutboundProcessor.sendOrderToExchange(order);
            ampsMessageOutboundProcessor.sendOrderToGUI(order);
            log.info("Order {} transitioned from {} to {} due to event {}", order.getOrderId(), currentState, newState, actionEvent);
        }
        else
            log.warn("No valid transition for order {} from state {} with event {}", order.getOrderId(), currentState, actionEvent);
    }

    private void applyAggregation(Order order)
    {
        if(Order.isParentOrder(order))
            orderAggregationService.updateParent(order);
        else
            orderAggregationService.updateChild(order);
    }

    private void processOrder(Order order)
    {
        if(order.getState() == NEW_ORDER && order.getActionEvent() == OrderStateEvents.SUBMIT_TO_OMS && Order.isParentOrder(order))
        {
            applyActionEvent(order, order.getActionEvent());
            applyActionEvent(order, OrderStateEvents.OMS_ACCEPT);
            orderAggregationService.updateParent(order);
            ampsMessageOutboundProcessor.sendOrderToGUI(order);
            log.info("Parent order {} submitted to OMS", order.getOrderId());
            return;
        }

        if(order.getState() == ACCEPTED_BY_OMS && order.getActionEvent() == OrderStateEvents.DESK_APPROVE && Order.isParentOrder(order))
        {
            applyActionEvent(order, order.getActionEvent());
            return;
        }

        if(order.getState() == ACCEPTED_BY_DESK && order.getActionEvent() == OrderStateEvents.SUBMIT_TO_EXCH && Order.isChildOrder(order))
        {
            applyActionEvent(order, order.getActionEvent());
            orderAggregationService.updateChild(order);
            ampsMessageOutboundProcessor.sendOrderToExchange(order);
            log.info("Child order {} submitted to exchange", order.getOrderId());
            return;
        }

        applyAggregation(order);
    }
} 