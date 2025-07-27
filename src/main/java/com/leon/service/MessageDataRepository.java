package com.leon.service;

import com.leon.model.MessageData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MessageDataRepository extends MongoRepository<MessageData, String>
{
    List<MessageData> findByTradeDateBetween(LocalDate startTradeDate, LocalDate endTradeDate);
    List<MessageData> findByTradeDate(LocalDate tradeDate);
}