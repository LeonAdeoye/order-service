package com.leon.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
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
    private double quantity;
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
    private String state;
    private String arrivalTime;
    private double arrivalPrice;
    private double averagePrice;
    private double adv20;
    private String executionTrigger;
    private double pending;
    private double executed;
    private double executedNotionalValue;
    private double orderNotionalValue;
    private double orderNotionalValueInLocal;
    private double residualNotionalValue;
    private double ivwap;
    private double performanceVsArrival;
    private double performanceVsArrivalBPS;
    private double performanceVsIVWAP;
    private double performanceVsIVWAPBPS;
} 