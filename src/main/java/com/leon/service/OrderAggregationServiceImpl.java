package com.leon.service;

import com.leon.model.Order;
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
    public void addParent(Order parent)
    {
        parents.put(parent.getOrderId(), parent);
    }

    @Override
    public void addChild(Order child)
    {
        children.put(child.getOrderId(), child);
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
    public void updateParent(Order parent)
    {
        if (parent != null && parent.getOrderId() != null) {
            parents.put(parent.getOrderId(), parent);
        }
    }

    @Override
    public void updateChild(Order child)
    {
        if (child != null && child.getOrderId() != null) {
            children.put(child.getOrderId(), child);
        }
        // update parent
    }
}
