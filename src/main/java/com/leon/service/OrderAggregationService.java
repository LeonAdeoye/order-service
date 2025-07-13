package com.leon.service;

import com.leon.model.Order;

public interface OrderAggregationService
{
    void addParent(Order parentOrder);
    void addChild(Order childOrder);
    Order getParent(String orderId);
    Order getChild(String orderId);
    void updateParent(Order parentOrder);
    void updateChild(Order childOrder);
    Order getParentOrder(Order childOrder);
}
