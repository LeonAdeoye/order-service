package com.leon.messaging;

import com.crankuptheamps.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.leon.model.Order;
import com.leon.service.OrderManagementService;
import com.leon.validator.OrderMessageValidator;
import com.leon.validator.ValidationResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AmpsMessageInboundProcessor implements MessageHandler
{
    private static final Logger log = LoggerFactory.getLogger(AmpsMessageInboundProcessor.class);
    @Value("${amps.server.url}")
    private String ampsServerUrl;
    @Value("${amps.client.name}")
    private String ampsClientName;
    @Value("${amps.topic.outbound.gui}")
    private String outboundGUITopic;
    @Value("${amps.topic.outbound.exchange}")
    private String outboundExchangeTopic;
    @Autowired
    private final OrderManagementService orderManagementService;
    private ObjectMapper objectMapper;
    @Autowired
    private final OrderMessageValidator messageValidator;
    private Client ampsClient;
    private CommandId outboundExchangeTopicId;
    private CommandId outboundGUITopicId;

    @PostConstruct
    public void initialize() throws Exception
    {
        try
        {
            ampsClient = new Client(ampsClientName);
            ampsClient.connect(ampsServerUrl);
            ampsClient.logon();
            objectMapper = new ObjectMapper();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH);
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
            javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
            objectMapper.registerModule(javaTimeModule);
            outboundGUITopicId = ampsClient.executeAsync(new Command("subscribe").setTopic(outboundGUITopic), this);
            outboundExchangeTopicId = ampsClient.executeAsync(new Command("subscribe").setTopic(outboundExchangeTopic), this);
        }
        catch (Exception e)
        {
            log.error("ERR-007: Failed to initialize AMPS client", e);
            throw e;
        }
    }

    @PreDestroy
    public void shutdown()
    {
        try
        {
            if (ampsClient != null)
            {
                ampsClient.unsubscribe(outboundGUITopicId);
                ampsClient.unsubscribe(outboundExchangeTopicId);
                ampsClient.disconnect();
                log.info("Unsubscribed from AMPS topics and disconnected.");
            }
        }
        catch (Exception e)
        {
            log.error("ERR-010: Failed to unsubscribe or disconnect from AMPS", e);
        }
    }

    @Override
    public void invoke(Message message)
    {
        try
        {
            ValidationResult validationResult = messageValidator.validateMessage(message.getData());
            if (!validationResult.valid())
            {
                log.error("ERR-008: Invalid message received: {}", validationResult.errorMessage());
                return;
            }
            Order order = objectMapper.readValue(message.getData(), Order.class);
            log.info("Processing valid message: {}", order);
            orderManagementService.processOrder(order);
        }
        catch (Exception e)
        {
            log.error("ERR-009: Failed to process message", e);
        }
    }
} 