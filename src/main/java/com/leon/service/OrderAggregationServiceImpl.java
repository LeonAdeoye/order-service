package com.leon.service;

import com.leon.model.MessageData;
import com.leon.model.OrderStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderAggregationServiceImpl implements OrderAggregationService
{
    private static final Logger logger = LoggerFactory.getLogger(OrderAggregationServiceImpl.class);
    private final Map<String, MessageData> parents = new HashMap<>();
    private final Map<String, MessageData> children = new HashMap<>();

    @Override
    public void addParent(MessageData parentMessageData)
    {
        parents.put(parentMessageData.getOrderId(), parentMessageData);
    }

    @Override
    public void addChild(MessageData childMessageData)
    {
        children.put(childMessageData.getOrderId(), childMessageData);
    }

    @Override
    public MessageData getParent(String orderId)
    {
        return parents.get(orderId);
    }

    @Override
    public MessageData getChildOrder(String orderId)
    {
        return children.get(orderId);
    }

    @Override
    public void updateParent(MessageData parentMessageData)
    {
        parents.put(parentMessageData.getOrderId(), parentMessageData);
    }

    @Override
    public void updateChild(MessageData childMessageData)
    {
        children.put(childMessageData.getOrderId(), childMessageData);
    }

    public MessageData getParentOrder(MessageData childMessageData)
    {
        return parents.get(childMessageData.getParentOrderId());
    }

    @Override
    public List<MessageData> getAllChildren(MessageData parentMessageData)
    {
        return children.values().stream()
                .filter(child -> child.getParentOrderId().equals(parentMessageData.getOrderId()))
                .collect(Collectors.toList());
    }

    public void aggregate(MessageData childOrderMessageData, MessageData executionMessageData)
    {
        int executedDelta = executionMessageData.getQuantity();
        if (executedDelta > 0)
        {
            childOrderMessageData.setExecuted(childOrderMessageData.getExecuted() + executedDelta);
            childOrderMessageData.setPending(childOrderMessageData.getPending() - executedDelta);

            if(MessageData.isFullyFilled(childOrderMessageData))
                childOrderMessageData.setState(OrderStates.FULLY_FILLED);

            if(MessageData.isPartiallyFilled(childOrderMessageData))
                childOrderMessageData.setState(OrderStates.PARTIALLY_FILLED);

            MessageData parentOrderMessageData = getParentOrder(childOrderMessageData);
            parentOrderMessageData.setPending(parentOrderMessageData.getPending() - executedDelta);
            parentOrderMessageData.setExecuted(parentOrderMessageData.getExecuted() + executedDelta);

            parentOrderMessageData.setResidualNotionalValueInLocal(parentOrderMessageData.getPending() * parentOrderMessageData.getPrice());
            parentOrderMessageData.setExecutedNotionalValueInLocal(parentOrderMessageData.getExecuted() * parentOrderMessageData.getPrice());
            childOrderMessageData.setResidualNotionalValueInLocal(childOrderMessageData.getPending() * childOrderMessageData.getPrice());
            childOrderMessageData.setExecutedNotionalValueInLocal(childOrderMessageData.getExecuted() * childOrderMessageData.getPrice());

            if (childOrderMessageData.getExecuted() > 0)
                childOrderMessageData.setAveragePrice(childOrderMessageData.getExecutedNotionalValueInLocal() / childOrderMessageData.getExecuted());

            if (parentOrderMessageData.getExecuted() > 0)
                parentOrderMessageData.setAveragePrice(parentOrderMessageData.getExecutedNotionalValueInLocal() / parentOrderMessageData.getExecuted());

            if(MessageData.isFullyFilled(parentOrderMessageData))
                parentOrderMessageData.setState(OrderStates.FULLY_FILLED);

            else if(MessageData.isPartiallyFilled(parentOrderMessageData))
                parentOrderMessageData.setState(OrderStates.PARTIALLY_FILLED);

            parents.put(parentOrderMessageData.getOrderId(), parentOrderMessageData);
            children.put(childOrderMessageData.getOrderId(), childOrderMessageData);
            logger.info("Updated parent order {} and child order {} using execution: {}", parentOrderMessageData.getOrderId(), childOrderMessageData.getOrderId(), executionMessageData.getOrderId());
        }
    }
}
