package com.leon.service;

import com.leon.model.Order;
import com.leon.model.OrderStates;

import java.util.Optional;

public interface OrderService {
    void saveOrder(Order orderToSave);
    Optional<Order> getOrderById(String orderId);
}
