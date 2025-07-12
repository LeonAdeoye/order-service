package com.leon.service;

import com.leon.model.Order;

public interface OrderAggregationService
{
    void addParent(Order parent);
    void addChild(Order child);
    Order getParent(String orderId);
    Order getChild(String orderId);
    void updateParent(Order parent);
    void updateChild(Order child);
}
