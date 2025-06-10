package com.leon.service;

import com.lmax.disruptor.EventFactory;
import com.leon.model.OrderEvent;

public class OrderEventFactory implements EventFactory<OrderEvent> {
    @Override
    public OrderEvent newInstance() {
        return new OrderEvent();
    }
} 