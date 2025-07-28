package com.leon.service;

import com.leon.model.MessageData;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderService
{
    void saveOrder(MessageData messageDataToSave);
    Optional<MessageData> getOrderById(String orderId);
    List<MessageData> getHistory(LocalDate startDate, LocalDate endDate, String clientCode, String instrumentCode);
    List<MessageData> getCrosses();
}
