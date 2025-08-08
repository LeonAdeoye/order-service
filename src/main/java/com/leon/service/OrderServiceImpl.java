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
    public List<MessageData> getHistory(LocalDate startTradeDate, LocalDate endTradeDate, String clientCode, String instrumentCode, String ownerId)
    {
        List<MessageData> messageData = messageDataRepository.findByTradeDateBetween(startTradeDate, endTradeDate)
            .stream().filter(message -> message.getState() == OrderStates.DONE_FOR_DAY
                && message.getMessageType() == MessageType.PARENT_ORDER
                && (clientCode.isEmpty() || message.getClientCode().equalsIgnoreCase(clientCode))
                && (ownerId.isEmpty() || message.getOwnerId().equalsIgnoreCase(ownerId))
                && (instrumentCode.isEmpty() || message.getInstrumentCode().equalsIgnoreCase(instrumentCode))).toList();


        logger.info("Retrieved {} orders from {} to {} for clientCode: {} and instrumentCode: {} and ownerId: {}",
            messageData.size(), startTradeDate, endTradeDate, clientCode, instrumentCode, ownerId);

        if (messageData.isEmpty())
            logger.warn("No orders found in the specified date range: {} to {} for clientCode: {} and instrumentCode: {} and ownerId: {}", startTradeDate, endTradeDate, clientCode, instrumentCode, ownerId);
        else
            logger.info("Retrieved {} orders from {} to {} for clientCode: {} and instrumentCode: {} and ownerId: {}", messageData.size(), startTradeDate, endTradeDate, clientCode, instrumentCode, ownerId);

        return messageData;
    }

    @Override
    public List<MessageData> getCrosses()
    {
        List<MessageData> messageData = messageDataRepository.findByTradeDate(LocalDate.now())
            .stream().filter(message -> (
                message.getState() != OrderStates.FULLY_FILLED &&
                message.getState() != OrderStates.REJECTED_BY_OMS &&
                message.getState() != OrderStates.REJECTED_BY_DESK &&
                message.getState() != OrderStates.REJECTED_BY_EXCH &&
                message.getState() != OrderStates.CANCELLED_BY_EXCH &&
                message.getState() != OrderStates.CANCELLED_BY_DESK &&
                message.getState() != OrderStates.DONE_FOR_DAY &&
                message.getMessageType() == MessageType.PARENT_ORDER)).toList();

        if (messageData.isEmpty())
            logger.warn("No crossing orders found with today's trade date.");

        return messageData;
    }

    @Override
    public List<MessageData> getInsights(String insightType) {
        return null;
    }

    @Override
    public void saveOrder(MessageData messageDataToSave)
    {
        messageDataToSave.setVersion(messageDataToSave.getVersion() + 1);
        orderCache.addOrder(messageDataToSave);
        dbTaskExecutor.execute(() -> messageDataRepository.save(messageDataToSave));
    }
}
