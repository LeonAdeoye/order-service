package com.leon.service;

import com.leon.messaging.AmpsMessageOutboundProcessor;
import com.leon.model.MessageData;
import com.leon.model.OrderStateEvents;
import com.leon.model.OrderStates;
import com.lmax.disruptor.EventHandler;
import com.leon.model.MessageEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import static com.leon.model.OrderStates.*;

@Component
@RequiredArgsConstructor
public class MessageEventHandler implements EventHandler<MessageEvent>
{
    private static final Logger log = LoggerFactory.getLogger(MessageEventHandler.class);
    @Autowired
    private OrderService orderService;
    @Autowired
    private AmpsMessageOutboundProcessor ampsMessageOutboundProcessor;
    @Autowired
    private OrderStateMachine orderStateMachine;
    @Autowired
    private OrderAggregationService orderAggregationService;

    @Override
    public void onEvent(MessageEvent event, long sequence, boolean endOfBatch)
    {
        try
        {
            process(event.getMessageData());
        }
        catch(Exception e)
        {
            log.error("Error processing order event: {}", event, e);
        }
    }

    public void transitionToNewState(MessageData messageData, OrderStateEvents actionEvent)
    {
        OrderStates currentState = messageData.getState();
        Optional<OrderStates> nextStateOpt = OrderStateMachine.getNextState(currentState, actionEvent);

        if (nextStateOpt.isPresent())
        {
            OrderStates newState = nextStateOpt.get();
            messageData.setState(newState);
            orderService.saveOrder(messageData);
            log.info("Order {} transitioned from {} to {} due to event {}", messageData.getOrderId(), currentState, newState, actionEvent);
        }
        else
            log.warn("No valid transition for order {} from state {} with event {}", messageData.getOrderId(), currentState, actionEvent);
    }

    private void process(MessageData messageData)
    {
        if(MessageData.isExecution(messageData))
            processExecution(messageData);
        else
            processOrder(messageData);
    }

    private void processExecution(MessageData executionMessageData)
    {
        log.info("Execution received for processing with id: {}", executionMessageData.getOrderId());
        MessageData childOrderMessageData = orderAggregationService.getChildOrder(executionMessageData.getParentOrderId());
        orderAggregationService.aggregate(childOrderMessageData, executionMessageData);
        MessageData parentOrderMessageData = orderAggregationService.getParentOrder(childOrderMessageData);
        orderService.saveOrder(executionMessageData);
        orderService.saveOrder(childOrderMessageData);
        orderService.saveOrder(parentOrderMessageData);
        ampsMessageOutboundProcessor.sendMessageToGUI(childOrderMessageData);
        ampsMessageOutboundProcessor.sendMessageToGUI(parentOrderMessageData);
    }

    private void processOrder(MessageData messageData)
    {
        log.info("{} order received for processing with id: {}", (MessageData.isParentOrder(messageData) ? "Parent" : "Child"), messageData.getOrderId());
        if (messageData.getState() == NEW_ORDER && messageData.getActionEvent() == OrderStateEvents.SUBMIT_TO_OMS && MessageData.isParentOrder(messageData))
        {
            transitionToNewState(messageData, messageData.getActionEvent());
            ampsMessageOutboundProcessor.sendMessageToGUI(messageData);
            transitionToNewState(messageData, OrderStateEvents.OMS_ACCEPT);
            orderAggregationService.updateParent(messageData);
            ampsMessageOutboundProcessor.sendMessageToGUI(messageData);
            return;
        }
        if (messageData.getState() == ACCEPTED_BY_OMS && messageData.getActionEvent() == OrderStateEvents.DESK_APPROVE && MessageData.isParentOrder(messageData))
        {
            transitionToNewState(messageData, messageData.getActionEvent());
            orderAggregationService.updateParent(messageData);
            ampsMessageOutboundProcessor.sendMessageToGUI(messageData);
            return;
        }
        if (messageData.getState() == ACCEPTED_BY_DESK && messageData.getActionEvent() == OrderStateEvents.SUBMIT_TO_EXCH && MessageData.isChildOrder(messageData))
        {
            transitionToNewState(messageData, messageData.getActionEvent());
            orderAggregationService.addChild(messageData);
            ampsMessageOutboundProcessor.sendMessageToExchange(messageData);
            return;
        }
        if (messageData.getState() == ACCEPTED_BY_EXCH && MessageData.isChildOrder(messageData))
        {
            orderAggregationService.updateChild(messageData);
            orderService.saveOrder(messageData);
            ampsMessageOutboundProcessor.sendMessageToGUI(messageData);
            return;
        }
        if ((messageData.getState() == FULLY_FILLED || messageData.getState() == PARTIALLY_FILLED) && MessageData.isParentOrder(messageData) && messageData.getActionEvent() == OrderStateEvents.DESK_DONE)
        {
            transitionToNewState(messageData, messageData.getActionEvent());
            orderAggregationService.updateParent(messageData);
            orderAggregationService.getAllChildren(messageData).forEach(child ->
            {
                child.setActionEvent(messageData.getActionEvent());
                ampsMessageOutboundProcessor.sendMessageToExchange(child);
                child.setState(messageData.getState());
                orderService.saveOrder(child);
                ampsMessageOutboundProcessor.sendMessageToGUI(child);
            });
            ampsMessageOutboundProcessor.sendMessageToGUI(messageData);
        }
    }
} 