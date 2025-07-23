package com.leon.service;

import com.leon.model.Order;
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
    private OrderRepository orderRepository;
    private final Executor dbTaskExecutor;
    @Autowired
    private OrderCache orderCache;

     @Override
    public Optional<Order> getOrderById(String orderId)
    {
        return Optional.ofNullable(orderCache.getOrder(orderId))
            .or(() ->
            {
                logger.info("Order not found in cache, fetching from database: {}", orderId);
                return orderRepository.findById(orderId);
            });
    }

    @Override
    public List<Order> getHistory(LocalDate startTradeDate, LocalDate endTradeDate)
    {
        List<Order> orders = orderRepository.findByTradeDateBetween(startTradeDate, endTradeDate)
                .stream().filter(order -> order.getState() == OrderStates.DONE_FOR_DAY).toList();

        if (orders.isEmpty())
            logger.warn("No orders found in the specified date range: {} to {}", startTradeDate, endTradeDate);

        return orders;
    }

    @Override
    public void saveOrder(Order orderToSave)
    {
        orderCache.addOrder(orderToSave);
        dbTaskExecutor.execute(() -> orderRepository.save(orderToSave));
    }
}
