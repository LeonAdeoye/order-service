package com.leon.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "Orders")
public class Order
{
    private static final Logger log = LoggerFactory.getLogger(Order.class);

    @Id
    private String orderId;
    private String instrumentCode;
    private String instrumentDescription;
    private String assetType;
    private String blgCode;
    private String ric;
    private String settlementCurrency;
    private String settlementType;
    private String exchangeAcronym;
    private String side;
    private int quantity;
    private String priceType;
    private double price;
    private String tif;
    private String traderInstruction;
    private String qualifier;
    private String destination;
    private String accountMnemonic;
    private String accountName;
    private String legalEntity;
    private boolean isFirmAccount;
    private boolean isRiskAccount;
    private String customFlags;
    private String brokerAcronym;
    private String brokerDescription;
    private String handlingInstruction;
    private String algoType;
    private boolean facilConsent;
    private String facilConsentDetails;
    private String facilInstructions;
    private int lotSize;
    private String clientCode;
    private String clientDescription;
    private String ownerId;
    private OrderStates state;
    @JsonFormat(pattern = "hh:mm:ss a")
    private LocalTime arrivalTime;
    private double arrivalPrice;
    private double averagePrice;
    private double adv20;
    private String executionTrigger;
    private int pending;
    private int executed;
    private double executedNotionalValueInUSD;
    private double orderNotionalValueInUSD;
    private double orderNotionalValueInLocal;
    private double residualNotionalValueInUSD;
    private double residualNotionalValueInLocal;
    private double ivwap;
    private double performanceVsArrival;
    private double performanceVsArrivalBPS;
    private double performanceVsIVWAP;
    private double performanceVsIVWAPBPS;
    private OrderStateEvents actionEvent;

    public String toJSON()
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            log.error("Failed to convert Order to JSON: {}", this, e);
            throw new RuntimeException("Failed to convert Order to JSON", e);
        }
    }
}
