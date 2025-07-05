package com.leon.service;

import com.leon.model.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderCache
{
    private final Map<String, Order> orderMap = new ConcurrentHashMap<>();

    public void addOrder(Order order)
    {
        orderMap.put(order.getOrderId(), order);
    }

    public Order getOrder(String orderId)
    {
        return orderMap.get(orderId);
    }

    public Map<String, Order> getAllOrders()
    {
        return orderMap;
    }

    public void clearCache()
    {
        orderMap.clear();
    }

    public void updateOrder(Order order)
    {
        if (orderMap.containsKey(order.getOrderId()))
            orderMap.put(order.getOrderId(), order);
        else
            throw new IllegalArgumentException("Order not found in cache: " + order.getOrderId());
    }
}
