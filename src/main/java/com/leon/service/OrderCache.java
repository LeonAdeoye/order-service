package com.leon.service;

import com.leon.model.MessageData;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderCache
{
    private final Map<String, MessageData> orderMap = new ConcurrentHashMap<>();

    public void addOrder(MessageData messageData)
    {
        orderMap.put(messageData.getOrderId(), messageData);
    }

    public MessageData getOrder(String orderId)
    {
        return orderMap.get(orderId);
    }

    public Map<String, MessageData> getAllOrders()
    {
        return orderMap;
    }

    public void clearCache()
    {
        orderMap.clear();
    }

    public void updateOrder(MessageData messageData)
    {
        if (orderMap.containsKey(messageData.getOrderId()))
            orderMap.put(messageData.getOrderId(), messageData);
        else
            throw new IllegalArgumentException("Order not found in cache: " + messageData.getOrderId());
    }
}
