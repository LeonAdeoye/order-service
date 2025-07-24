package com.leon.service;

import com.lmax.disruptor.EventFactory;
import com.leon.model.MessageEvent;

public class MessageEventFactory implements EventFactory<MessageEvent>
{
    @Override
    public MessageEvent newInstance()
    {
        return new MessageEvent();
    }
} 