package com.leon.model;

public enum OrderStates {
    NEW("New Order"),
    PENDING_NEW("Pending New Order"),
    NEW_ACK("New Order Acknowledged"),
    REJECTED_BY_OMS("Rejected By OMS"),
    ACCEPTED_BY_DESK("Accepted By Desk"),
    REJECTED_BY_DESK("Rejected By Desk"),
    SENT_TO_EXCHANGE("Sent to Exchange"),
    ACKNOWLEDGED_BY_EXCHANGE("Acknowledged by Exchange"),
    REJECTED_BY_EXCHANGE("Rejected By Exchange");

    private final String orderStateDescription;

    OrderStates(String orderStateDescription)
    {
        this.orderStateDescription = orderStateDescription;
    }

    public String getOrderStateDescription()
    {
        return orderStateDescription;
    }
}
