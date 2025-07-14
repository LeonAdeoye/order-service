package com.leon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "Orders")
public class Order
{
    private static final Logger log = LoggerFactory.getLogger(Order.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH);
    private static final ObjectMapper MAPPER;
    static
    {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(javaTimeModule);
    }

    @Id
    private String orderId;
    private String parentOrderId;
    @JsonProperty("isFirmAccount")
    private boolean isFirmAccount;
    @JsonProperty("isRiskAccount")
    private boolean isRiskAccount;
    private String instrumentCode;
    private String instrumentDescription;
    private String assetType;
    private String blgCode;
    private String ric;
    private String settlementCurrency;
    private String settlementType;
    private String exchangeAcronym;
    private Side side;
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
    private LocalDate tradeDate;
    private LocalTime arrivalTime;
    private double arrivalPrice;
    private double averagePrice;
    private double adv20;
    private String executionTrigger;
    private int pending;
    private int executed;
    private int cumulativeExecuted;
    private double executedNotionalValueInUSD;
    private double executedNotionalValueInLocal;
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
    private double percentageOfParentOrder;
    private String originalSource;
    private String currentSource;
    private String targetSource;

    public Order(Order newOrder)
    {
        this.orderId = newOrder.getOrderId();
        this.parentOrderId = newOrder.getParentOrderId();
        this.isFirmAccount = newOrder.isFirmAccount();
        this.isRiskAccount = newOrder.isRiskAccount();
        this.instrumentCode = newOrder.getInstrumentCode();
        this.instrumentDescription = newOrder.getInstrumentDescription();
        this.assetType = newOrder.getAssetType();
        this.blgCode = newOrder.getBlgCode();
        this.ric = newOrder.getRic();
        this.settlementCurrency = newOrder.getSettlementCurrency();
        this.settlementType = newOrder.getSettlementType();
        this.exchangeAcronym = newOrder.getExchangeAcronym();
        this.side = newOrder.getSide();
        this.quantity = newOrder.getQuantity();
        this.pending = newOrder.getPending();
        this.executed = newOrder.getExecuted();
        this.cumulativeExecuted = newOrder.getCumulativeExecuted();
        this.priceType = newOrder.getPriceType();
        this.price = newOrder.getPrice();
        this.tif = newOrder.getTif();
        this.traderInstruction = newOrder.getTraderInstruction();
        this.qualifier = newOrder.getQualifier();
        this.destination = newOrder.getDestination();
        this.accountMnemonic = newOrder.getAccountMnemonic();
        this.accountName = newOrder.getAccountName();
        this.legalEntity = newOrder.getLegalEntity();
        this.customFlags = newOrder.getCustomFlags();
        this.brokerAcronym = newOrder.getBrokerAcronym();
        this.brokerDescription = newOrder.getBrokerDescription();
        this.handlingInstruction = newOrder.getHandlingInstruction();
        this.algoType = newOrder.getAlgoType();
        this.facilConsent = newOrder.isFacilConsent();
        this.facilConsentDetails = newOrder.getFacilConsentDetails();
        this.facilInstructions = newOrder.getFacilInstructions();
        this.lotSize = newOrder.getLotSize();
        this.clientCode = newOrder.getClientCode();
        this.clientDescription = newOrder.getClientDescription();
        this.ownerId = newOrder.getOwnerId();
        this.state = newOrder.getState();
        this.tradeDate = newOrder.getTradeDate();
        this.arrivalTime = newOrder.getArrivalTime();
        this.arrivalPrice = newOrder.getArrivalPrice();
        this.averagePrice = newOrder.getAveragePrice();
        this.adv20 = newOrder.getAdv20();
        this.executionTrigger = newOrder.getExecutionTrigger();
        this.executedNotionalValueInUSD = newOrder.getExecutedNotionalValueInUSD();
        this.executedNotionalValueInLocal = newOrder.getExecutedNotionalValueInLocal();
        this.orderNotionalValueInUSD = newOrder.getOrderNotionalValueInUSD();
        this.orderNotionalValueInLocal = newOrder.getOrderNotionalValueInLocal();
        this.residualNotionalValueInUSD = newOrder.getResidualNotionalValueInUSD();
        this.residualNotionalValueInLocal = newOrder.getResidualNotionalValueInLocal();
        this.ivwap = newOrder.getIvwap();
        this.performanceVsArrival = newOrder.getPerformanceVsArrival();
        this.performanceVsArrivalBPS = newOrder.getPerformanceVsArrivalBPS();
        this.performanceVsIVWAP = newOrder.getPerformanceVsIVWAP();
        this.performanceVsIVWAPBPS = newOrder.getPerformanceVsIVWAPBPS();
        this.actionEvent = newOrder.getActionEvent();
        this.percentageOfParentOrder = newOrder.getPercentageOfParentOrder();
        this.originalSource = newOrder.getOriginalSource();
        this.currentSource = newOrder.getCurrentSource();
        this.targetSource = newOrder.getTargetSource();
    }


    public static boolean isParentOrder(Order order)
    {
        return order.getParentOrderId().equals(order.getOrderId());
    }
    public static boolean isChildOrder(Order order)
    {
        return !order.getParentOrderId().equals(order.getOrderId());
    }

    public static boolean isFullyFilled(Order parentOrder)
    {
        return parentOrder.getPending() == 0 && parentOrder.getExecuted() == parentOrder.getQuantity();
    }

    public static boolean isPartiallyFilled(Order parentOrder)
    {
        return parentOrder.getPending() > 0 && parentOrder.getExecuted() > 0 && parentOrder.getExecuted() < parentOrder.getQuantity();
    }

    public String toJSON()
    {
        try
        {
            return MAPPER.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            log.error("Failed to convert Order to JSON: {}", this, e);
            throw new RuntimeException("Failed to convert Order to JSON", e);
        }
    }
}
