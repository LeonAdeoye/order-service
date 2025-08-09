package com.leon.service;

import com.leon.model.InsightItem;
import com.leon.model.MessageData;
import com.leon.model.MessageType;
import com.leon.model.OrderStates;
import com.leon.model.Side;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<InsightItem> getInsights(String insightType, String metric, LocalDate startDate, LocalDate endDate)
    {
        List<MessageData> ordersInRange = messageDataRepository.findByTradeDateBetween(startDate, endDate)
            .stream().filter(message -> message.getMessageType() == MessageType.PARENT_ORDER).toList();

        if (ordersInRange.isEmpty())
        {
            logger.warn("No insights found with trade date range.");
            return new ArrayList<>();
        }

        logger.info("Retrieved {} records for insights. type={}, metric={}, startDate={}, endDate={}",
            ordersInRange.size(), insightType, metric, startDate, endDate);

        return convertOrdersToInsightItems(ordersInRange, insightType, metric);
    }

    private List<InsightItem> convertOrdersToInsightItems(List<MessageData> orders, String insightType, String metric)
    {
        Map<String, InsightItem> grouping = new HashMap<>();

        for (MessageData order : orders)
        {
            String name = switch (insightType == null ? "" : insightType.toLowerCase())
            {
                case "client" -> defaultIfBlank(order.getClientCode(), order.getClientDescription());
                case "sector" -> defaultIfBlank(order.getInstrumentCode(), "Unknown");
                case "country" -> defaultIfBlank(order.getSettlementCurrency(), "Unknown");
                case "instrument" -> defaultIfBlank(order.getInstrumentCode(), order.getInstrumentDescription());
                default -> "Unknown";
            };

            InsightItem aggregated = grouping.computeIfAbsent(name, k -> InsightItem.builder()
                .name(k)
                .orderBuy(0)
                .executedBuy(0)
                .orderSell(0)
                .executedSell(0)
                .build());

            double[] values = extractMetricValues(order, metric);
            double orderValue = values[0];
            double executedValue = values[1];

            if (order.getSide() == Side.BUY)
            {
                aggregated.setOrderBuy(aggregated.getOrderBuy() + orderValue);
                aggregated.setExecutedBuy(aggregated.getExecutedBuy() + executedValue);
            }
            else
            {
                aggregated.setOrderSell(aggregated.getOrderSell() - orderValue);
                aggregated.setExecutedSell(aggregated.getExecutedSell() - executedValue);
            }
        }

        return new ArrayList<>(grouping.values());
    }

    private double[] extractMetricValues(MessageData order, String metric)
    {
        String normalized = metric == null ? "shares" : metric.trim().toLowerCase();
        switch (normalized)
        {
            case "notionalUSD":
                return new double[] { safeDouble(order.getOrderNotionalValueInUSD()), safeDouble(order.getExecutedNotionalValueInUSD()) };
            case "notionalLocal":
                return new double[] { safeDouble(order.getOrderNotionalValueInLocal()), safeDouble(order.getExecutedNotionalValueInLocal()) };
            case "shares":
            default:
                return new double[] { order.getQuantity(), order.getExecuted() };
        }
    }

    private String defaultIfBlank(String value, String fallback)
    {
        if (value == null || value.isBlank()) return fallback == null ? "Unknown" : fallback;
        return value;
    }

    private double safeDouble(Double value)
    {
        return value == null ? 0d : value;
    }

    @Override
    public void saveOrder(MessageData messageDataToSave)
    {
        messageDataToSave.setVersion(messageDataToSave.getVersion() + 1);
        orderCache.addOrder(messageDataToSave);
        dbTaskExecutor.execute(() -> messageDataRepository.save(messageDataToSave));
    }
}
