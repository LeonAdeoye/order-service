package com.leon.service;

import com.lmax.disruptor.EventHandler;
import com.leon.model.Order;
import com.leon.model.OrderEvent;

public interface DisruptorService
{
    void start(String name, EventHandler<OrderEvent> actionEventHandler);
    void stop();
    void push(Order order);
} 