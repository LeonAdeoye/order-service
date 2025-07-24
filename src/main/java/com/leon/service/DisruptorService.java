package com.leon.service;

import com.leon.model.MessageData;
import com.lmax.disruptor.EventHandler;
import com.leon.model.MessageEvent;

public interface DisruptorService
{
    void start(String name, EventHandler<MessageEvent> actionEventHandler);
    void stop();
    void push(MessageData messageData);
} 