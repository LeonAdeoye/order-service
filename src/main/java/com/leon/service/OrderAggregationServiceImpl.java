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
        return children.values().stream().filter(child -> child.getParentOrderId().equals(parentMessageData.getOrderId())).collect(Collectors.toList());
    }

    @Override
    public void aggregate(MessageData parentOrderMessageData, MessageData childOrderMessageData, MessageData executionMessageData)
    {
        int executedDelta = executionMessageData.getExecuted();
        if (executedDelta > 0)
        {
            updateOrder(childOrderMessageData, executedDelta);
            children.put(childOrderMessageData.getOrderId(), childOrderMessageData);

            updateOrder(parentOrderMessageData, executedDelta);
            parents.put(parentOrderMessageData.getOrderId(), parentOrderMessageData);

            logger.info("Updated parent order {} and child order {} using execution: {} after quantity executed: {}",
                parentOrderMessageData.getOrderId(), childOrderMessageData.getOrderId(), executionMessageData.getOrderId(), executedDelta);
        }
    }

    private void updateOrder(MessageData order, int executedDelta)
    {
        order.setExecuted(order.getExecuted() + executedDelta);
        order.setPending(order.getPending() - executedDelta);
        order.setResidualNotionalValueInLocal(order.getPending() * order.getPrice());
        order.setExecutedNotionalValueInLocal(order.getExecuted() * order.getPrice());
        order.setAveragePrice(order.getExecutedNotionalValueInLocal() / order.getExecuted());
        order.setState(MessageData.isFullyFilled(order) ? OrderStates.FULLY_FILLED : OrderStates.PARTIALLY_FILLED);
    }
}
