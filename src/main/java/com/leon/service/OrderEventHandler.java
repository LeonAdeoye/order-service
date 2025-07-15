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
            process(event.getOrder());
        }
        catch(Exception e)
        {
            log.error("Error processing order event: {}", event, e);
        }
    }

    public void transitionToNewState(Order order, OrderStateEvents actionEvent)
    {
        OrderStates currentState = order.getState();
        Optional<OrderStates> nextStateOpt = OrderStateMachine.getNextState(currentState, actionEvent);

        if (nextStateOpt.isPresent())
        {
            OrderStates newState = nextStateOpt.get();
            order.setState(newState);
            orderService.saveOrder(order);
            log.info("Order {} transitioned from {} to {} due to event {}", order.getOrderId(), currentState, newState, actionEvent);
        }
        else
            log.warn("No valid transition for order {} from state {} with event {}", order.getOrderId(), currentState, actionEvent);
    }

    private void process(Order order)
    {
        if(Order.isExecution(order))
            processExecution(order);
        else
            processOrder(order);
    }

    private void processExecution(Order execution)
    {
        log.info("Execution received for processing with id: {}", execution.getOrderId());
        Order childOrder = orderAggregationService.getChild(execution.getParentOrderId());
        childOrder.setExecuted(childOrder.getExecuted() + execution.getQuantity());
        childOrder.setPending(childOrder.getPending() - execution.getQuantity());

        if(childOrder.getPending() == 0 && childOrder.getExecuted() == execution.getQuantity())
            transitionToNewState(childOrder, OrderStateEvents.FULL_FILL);

        if(childOrder.getPending() > 0 && childOrder.getExecuted() > 0 && childOrder.getExecuted() < childOrder.getQuantity())
            transitionToNewState(childOrder, OrderStateEvents.PARTIAL_FILL);

        orderService.saveOrder(execution);
        orderService.saveOrder(childOrder);
        orderAggregationService.updateChild(childOrder);
        ampsMessageOutboundProcessor.sendOrderToGUI(childOrder);
        ampsMessageOutboundProcessor.sendOrderToGUI(orderAggregationService.getParentOrder(childOrder));
    }

    private void processOrder(Order order) {
        log.info("{} order received for processing with id: {}", (Order.isParentOrder(order) ? "Parent" : "Child"), order.getOrderId());
        if (order.getState() == NEW_ORDER && order.getActionEvent() == OrderStateEvents.SUBMIT_TO_OMS && Order.isParentOrder(order)) {
            transitionToNewState(order, order.getActionEvent());
            ampsMessageOutboundProcessor.sendOrderToGUI(order);
            transitionToNewState(order, OrderStateEvents.OMS_ACCEPT);
            orderAggregationService.updateParent(order);
            ampsMessageOutboundProcessor.sendOrderToGUI(order);
            return;
        }

        if (order.getState() == ACCEPTED_BY_OMS && order.getActionEvent() == OrderStateEvents.DESK_APPROVE && Order.isParentOrder(order)) {
            transitionToNewState(order, order.getActionEvent());
            orderAggregationService.updateParent(order);
            ampsMessageOutboundProcessor.sendOrderToGUI(order);
            return;
        }

        if (order.getState() == ACCEPTED_BY_DESK && order.getActionEvent() == OrderStateEvents.SUBMIT_TO_EXCH && Order.isChildOrder(order)) {
            transitionToNewState(order, order.getActionEvent());
            orderService.saveOrder(order);
            orderAggregationService.addChild(order);
            ampsMessageOutboundProcessor.sendOrderToExchange(order);
        }

        if (order.getState() == ACCEPTED_BY_EXCH && Order.isChildOrder(order)) {
            orderAggregationService.updateChild(order);
            orderService.saveOrder(order);
            ampsMessageOutboundProcessor.sendOrderToGUI(order);
        }
    }
} 