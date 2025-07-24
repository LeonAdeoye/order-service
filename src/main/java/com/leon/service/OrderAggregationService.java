package com.leon.service;

import com.leon.model.MessageData;

import java.util.List;

public interface OrderAggregationService
{
    void addParent(MessageData parentMessageData);
    void addChild(MessageData childMessageData);
    MessageData getParent(String orderId);
    MessageData getChildOrder(String orderId);
    void updateParent(MessageData parentMessageData);
    void updateChild(MessageData childMessageData);
    List<MessageData> getAllChildren(MessageData parentMessageData);
    void aggregate(MessageData childMessageData);
    void aggregate(MessageData childMessageData, MessageData executionMessageData);
    MessageData getParentOrder(MessageData childMessageData);
}
