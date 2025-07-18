package com.leon.messaging;

import com.crankuptheamps.client.Client;
import com.leon.model.Order;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AmpsMessageOutboundProcessor
{
    private static final Logger log = LoggerFactory.getLogger(AmpsMessageOutboundProcessor.class);
    private Client ampsClient;
    @Value("${amps.server.url}")
    private String ampsServerUrl;
    @Value("${amps.client.name}")
    private String ampsClientName;
    @Value("${amps.topic.inbound.gui}")
    private String inboundGUITopic;
    @Value("${amps.topic.inbound.exchange}")
    private String inboundExchangeTopic;

    @PostConstruct
    public void initialize() throws Exception
    {
        try
        {
            ampsClient = new Client(ampsClientName);
            ampsClient.connect(ampsServerUrl);
            ampsClient.logon();
        }
        catch (Exception e)
        {
            log.error("ERR-901: Failed to initialize AMPS client for AmpsOutboundProcessor", e);
            throw e;
        }
    }

    public void sendOrderToGUI(Order order)
    {
        try
        {
            order.setCurrentSource("ORDER_MANAGEMENT_SERVICE");
            order.setTargetSource("WEB_TRADER");
            ampsClient.publish(inboundGUITopic, order.toJSON());
            log.info("Published order message to GUI: {}", order);
        }
        catch (Exception e)
        {
            log.error("ERR-902: Failed to publish order message to GUI: {}", order, e);
        }
    }

    public void sendOrderToExchange(Order order)
    {
        try
        {
            order.setCurrentSource("ORDER_MANAGEMENT_SERVICE");
            order.setTargetSource("EXCHANGE_SERVICE");
            ampsClient.publish(inboundExchangeTopic, order.toJSON());
            log.info("Published order message to exchange: {}", order);
        }
        catch (Exception e)
        {
            log.error("ERR-903: Failed to publish order message to exchange: {}", order, e);
        }
    }
}
