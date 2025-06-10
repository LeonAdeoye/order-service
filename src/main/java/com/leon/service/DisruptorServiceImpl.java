package com.leon.service;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.leon.model.Order;
import com.leon.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DisruptorServiceImpl implements DisruptorService {
    private static final Logger logger = LoggerFactory.getLogger(DisruptorServiceImpl.class);
    private int counter;
    private String name;
    private Disruptor<OrderEvent> disruptor;
    private DisruptorEventProducer producer;
    @Value("${buffer.size}")
    private int bufferSize;

    @Override
    public void start(String name, EventHandler<OrderEvent> actionEventHandler) {
        this.name = name;
        counter = 0;
        OrderEventFactory factory = new OrderEventFactory();
        disruptor = new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        disruptor.handleEventsWith(actionEventHandler);
        disruptor.start();
        logger.info("Started " + name + " disruptor.");
        RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();
        producer = new DisruptorEventProducer(ringBuffer);
        logger.info("Instantiated producer for " + name + " disruptor.");
    }

    @Override
    public void stop() {
        logger.info(counter + " events were processed by " + name + " disruptor");
        disruptor.halt();
        logger.info("Halted " + name + " disruptor");
        disruptor.shutdown();
        logger.info("Shutdown " + name + " disruptor");
    }

    @Override
    public void push(Order order) {
        producer.onData(order);
        counter++;
    }
} 