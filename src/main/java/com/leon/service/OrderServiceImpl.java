package com.leon.service;

import com.leon.model.MessageData;
import com.leon.model.MessageType;
import com.leon.model.OrderStates;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService
{
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private MessageDataRepository messageDataRepository;
    private final Executor dbTaskExecutor;
    @Autowired
    private OrderCache orderCache;

     @Override
    public Optional<MessageData> getOrderById(String orderId)
    {
        return Optional.ofNullable(orderCache.getOrder(orderId)).or(() ->
            {
                logger.info("Order not found in cache, fetching from database: {}", orderId);
                return messageDataRepository.findById(orderId);
            });
    }

    @Override
    public List<MessageData> getHistory(LocalDate startTradeDate, LocalDate endTradeDate)
    {
        List<MessageData> messageData = messageDataRepository.findByTradeDateBetween(startTradeDate, endTradeDate)
            .stream().filter(message -> message.getState() == OrderStates.DONE_FOR_DAY
            && message.getMessageType() == MessageType.PARENT_ORDER).toList();

        if (messageData.isEmpty())
            logger.warn("No orders found in the specified date range: {} to {}", startTradeDate, endTradeDate);

        return messageData;
    }

    @Override
    public void saveOrder(MessageData messageDataToSave)
    {
        messageDataToSave.setVersion(messageDataToSave.getVersion() + 1);
        orderCache.addOrder(messageDataToSave);
        dbTaskExecutor.execute(() -> messageDataRepository.save(messageDataToSave));
    }
}
