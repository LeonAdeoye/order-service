package com.leon.service;

import com.leon.model.Order;
import com.leon.model.OrderStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderAggregationServiceImpl implements OrderAggregationService
{
    private static final Logger logger = LoggerFactory.getLogger(OrderAggregationServiceImpl.class);
    private final Map<String, Order> parents = new HashMap<>();
    private final Map<String, Order> children = new HashMap<>();

    @Override
    public void addParent(Order parentOrder)
    {
        parents.put(parentOrder.getOrderId(), parentOrder);
    }

    @Override
    public void addChild(Order childOrder)
    {
        children.put(childOrder.getOrderId(), childOrder);
    }

    @Override
    public Order getParent(String orderId)
    {
        return parents.get(orderId);
    }

    @Override
    public Order getChild(String orderId)
    {
        return children.get(orderId);
    }

    @Override
    public void updateParent(Order parentOrder)
    {
        if (parentOrder != null && parentOrder.getOrderId() != null)
            parents.put(parentOrder.getOrderId(), parentOrder);
    }

    public Order getParentOrder(Order childOrder)
    {
        return parents.get(childOrder.getParentOrderId());
    }

    @Override
    public void updateChild(Order childOrder)
    {
        if (childOrder.getExecuted() > 0)
        {
            Order parentOrder = getParentOrder(childOrder);
            if (parentOrder != null)
            {
                parentOrder.setPending(parentOrder.getPending() - childOrder.getExecuted());
                parentOrder.setExecuted(parentOrder.getExecuted() + childOrder.getExecuted());

                parentOrder.setResidualNotionalValueInLocal(parentOrder.getPending() * parentOrder.getPrice());
                parentOrder.setExecutedNotionalValueInLocal(parentOrder.getExecuted() * parentOrder.getPrice());

                if (childOrder.getExecuted() > 0)
                    childOrder.setAveragePrice(childOrder.getExecutedNotionalValueInLocal() / childOrder.getExecuted());

                if (parentOrder.getExecuted() > 0)
                    parentOrder.setAveragePrice(parentOrder.getExecutedNotionalValueInLocal() / parentOrder.getExecuted());

                if(Order.isFullyFilled(parentOrder))
                    parentOrder.setState(OrderStates.FULLY_FILLED);
                else if(Order.isPartiallyFilled(parentOrder))
                    parentOrder.setState(OrderStates.PARTIALLY_FILLED);

                parents.put(parentOrder.getOrderId(), parentOrder);
                logger.info("Updated parent order {} from child order {}", parentOrder.getOrderId(), childOrder.getOrderId());
            }
            else
                logger.warn("Parent order with ID {} not found for child {}", parentOrder.getOrderId(), childOrder.getOrderId());
        }
        children.put(childOrder.getOrderId(), childOrder);
    }
}
