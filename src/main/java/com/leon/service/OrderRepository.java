package com.leon.service;

import com.leon.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String>
{
    List<Order> findByTradeDateBetween(LocalDate startTradeDate, LocalDate endTradeDate);
}