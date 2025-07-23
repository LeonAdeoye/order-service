package com.leon.service;

import com.leon.model.Order;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderService
{
    void saveOrder(Order orderToSave);
    Optional<Order> getOrderById(String orderId);
    List<Order> getHistory(LocalDate startDate, LocalDate endDate);
}
