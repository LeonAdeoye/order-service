package com.leon.messaging;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.model.Order;
import com.leon.service.OrderManagementService;
import com.leon.validator.OrderMessageValidator;
import com.leon.validator.ValidationResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AmpsMessageInboundProcessor implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(AmpsMessageInboundProcessor.class);

    @Value("${amps.server.url}")
    private String ampsServerUrl;
    @Value("${amps.client.name}")
    private String ampsClientName;
    @Value("${amps.topic.orders}")
    private String ordersTopic;
    @Autowired
    private final OrderManagementService orderManagementService;
    @Autowired
    private final ObjectMapper objectMapper;
    @Autowired
    private final OrderMessageValidator messageValidator;
    private Client ampsClient;

    @PostConstruct
    public void initialize() throws Exception {
        try {
            ampsClient = new Client(ampsClientName);
            ampsClient.connect(ampsServerUrl);
            ampsClient.logon();
            for(Message message : (ampsClient.subscribe(ordersTopic))) {
                invoke(message);
            }
        } catch (Exception e) {
            log.error("ERR-007: Failed to initialize AMPS client", e);
            throw e;
        }
    }

    @Override
    public void invoke(Message message) {
        String errorId = UUID.randomUUID().toString();
        MDC.put("errorId", errorId);

        try {
            ValidationResult validationResult = messageValidator.validateMessage(message.getData());

            if (!validationResult.valid()) {
                log.error("ERR-008: Invalid message received: {}", validationResult.errorMessage());
                return;
            }

            Order order = objectMapper.readValue(message.getData(), Order.class);
            log.info("Received valid order message: {}", order);
            orderManagementService.processOrder(order);

        } catch (Exception e) {
            log.error("ERR-009: Failed to process message", e);
        } finally {
            MDC.remove("errorId");
        }
    }
} 