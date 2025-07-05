package com.leon.service;

import com.leon.model.OrderStateEvents;

public interface OrderFiniteStateMachineService
{
    void sendEvent(String orderId, OrderStateEvents event);
}
