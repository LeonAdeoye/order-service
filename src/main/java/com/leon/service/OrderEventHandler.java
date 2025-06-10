package com.leon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lmax.disruptor.EventHandler;
import com.leon.model.Order;
import com.leon.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class OrderEventHandler implements EventHandler<OrderEvent> {
    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);
    @Autowired
    private final OrderPersistenceService orderPersistenceService;
    @Autowired
    private final CurrencyManager currencyManager;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final double ROUNDING_FACTOR = Math.pow(10, 2);
    private static final Function<Double, Double> round2dp = (value) -> Math.round(value * ROUNDING_FACTOR) / ROUNDING_FACTOR;

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        try {
            MDC.put("errorId", event.getErrorId());
            processOrder(event.getOrder());
        } finally {
            MDC.remove("errorId");
        }
    }

    private void processOrder(Order order) {
        // save to mongo DB database
        orderPersistenceService.save(order);
        log.info("Order saved: {}", order.getOrderId());
    }
} 