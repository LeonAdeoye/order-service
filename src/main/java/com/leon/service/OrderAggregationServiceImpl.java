package com.leon.service;

import com.leon.model.MessageData;
import com.leon.model.OrderStateEvents;
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

    public void aggregate(MessageData childMessageData)
    {
        MessageData parentMessageData = getParentOrder(childMessageData);
        if (parentMessageData != null && childMessageData.getExecuted() > 0)
        {
            parentMessageData.setPending(parentMessageData.getPending() - childMessageData.getExecuted());
            parentMessageData.setExecuted(parentMessageData.getExecuted() + childMessageData.getExecuted());

            parentMessageData.setResidualNotionalValueInLocal(parentMessageData.getPending() * parentMessageData.getPrice());
            parentMessageData.setExecutedNotionalValueInLocal(parentMessageData.getExecuted() * parentMessageData.getPrice());
            childMessageData.setResidualNotionalValueInLocal(childMessageData.getPending() * childMessageData.getPrice());
            childMessageData.setExecutedNotionalValueInLocal(childMessageData.getExecuted() * childMessageData.getPrice());

            if (childMessageData.getExecuted() > 0)
                childMessageData.setAveragePrice(childMessageData.getExecutedNotionalValueInLocal() / childMessageData.getExecuted());

            if (parentMessageData.getExecuted() > 0)
                parentMessageData.setAveragePrice(parentMessageData.getExecutedNotionalValueInLocal() / parentMessageData.getExecuted());

            if(MessageData.isFullyFilled(parentMessageData))
                parentMessageData.setState(OrderStates.FULLY_FILLED);
            else if(MessageData.isPartiallyFilled(parentMessageData))
                parentMessageData.setState(OrderStates.PARTIALLY_FILLED);

            parents.put(parentMessageData.getOrderId(), parentMessageData);
            logger.info("Updated parent order {} from child order {}", parentMessageData.getOrderId(), childMessageData.getOrderId());
        }
        children.put(childMessageData.getOrderId(), childMessageData);
    }

    public void aggregate(MessageData childMessageData, MessageData executionMessageData)
    {
        childMessageData.setExecuted(childMessageData.getExecuted() + executionMessageData.getQuantity());
        childMessageData.setPending(childMessageData.getPending() - executionMessageData.getQuantity());

        if(MessageData.isFullyFilled(childMessageData))
            childMessageData.setState(OrderStates.FULLY_FILLED);

        if(MessageData.isPartiallyFilled(childMessageData))
            childMessageData.setState(OrderStates.PARTIALLY_FILLED);

        MessageData parentMessageData = getParentOrder(childMessageData);
        if (parentMessageData != null && childMessageData.getExecuted() > 0)
        {
            parentMessageData.setPending(parentMessageData.getPending() - childMessageData.getExecuted());
            parentMessageData.setExecuted(parentMessageData.getExecuted() + childMessageData.getExecuted());

            parentMessageData.setResidualNotionalValueInLocal(parentMessageData.getPending() * parentMessageData.getPrice());
            parentMessageData.setExecutedNotionalValueInLocal(parentMessageData.getExecuted() * parentMessageData.getPrice());
            childMessageData.setResidualNotionalValueInLocal(childMessageData.getPending() * childMessageData.getPrice());
            childMessageData.setExecutedNotionalValueInLocal(childMessageData.getExecuted() * childMessageData.getPrice());

            if (childMessageData.getExecuted() > 0)
                childMessageData.setAveragePrice(childMessageData.getExecutedNotionalValueInLocal() / childMessageData.getExecuted());

            if (parentMessageData.getExecuted() > 0)
                parentMessageData.setAveragePrice(parentMessageData.getExecutedNotionalValueInLocal() / parentMessageData.getExecuted());

            if(MessageData.isFullyFilled(parentMessageData))
                parentMessageData.setState(OrderStates.FULLY_FILLED);
            else if(MessageData.isPartiallyFilled(parentMessageData))
                parentMessageData.setState(OrderStates.PARTIALLY_FILLED);

            parents.put(parentMessageData.getOrderId(), parentMessageData);
            logger.info("Updated parent order {} from child order {}", parentMessageData.getOrderId(), childMessageData.getOrderId());
        }
        children.put(childMessageData.getOrderId(), childMessageData);
    }
}
