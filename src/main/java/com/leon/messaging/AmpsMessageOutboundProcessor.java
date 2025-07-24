package com.leon.messaging;

import com.crankuptheamps.client.Client;
import com.leon.model.MessageData;
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

    public void sendMessageToGUI(MessageData messageData)
    {
        try
        {
            messageData.setCurrentSource("ORDER_MANAGEMENT_SERVICE");
            messageData.setTargetSource("WEB_TRADER");
            ampsClient.publish(inboundGUITopic, messageData.toJSON());
            log.info("Published message to GUI: {}", messageData);
        }
        catch (Exception e)
        {
            log.error("ERR-902: Failed to publish message to GUI: {}", messageData, e);
        }
    }

    public void sendMessageToExchange(MessageData messageData)
    {
        try
        {
            messageData.setCurrentSource("ORDER_MANAGEMENT_SERVICE");
            messageData.setTargetSource("EXCHANGE_SERVICE");
            ampsClient.publish(inboundExchangeTopic, messageData.toJSON());
            log.info("Published message to exchange: {}", messageData);
        }
        catch (Exception e)
        {
            log.error("ERR-903: Failed to publish message to exchange: {}", messageData, e);
        }
    }
}
