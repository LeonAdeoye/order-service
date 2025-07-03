package com.leon.model;

public enum OrderStateEvent {
    SUBMIT_TO_DESK("Submit To desk"),
    OMS_ACCEPT("OMS Accept"),
    OMS_REJECT("OMS Reject"),
    DESK_APPROVE("Desk Approve"),
    DESK_REJECT("Desk Reject"),
    SUBMIT_TO_EXCHANGE("Submit To Exchange"),
    EXCHANGE_ACKNOWLEDGE("Exchange Acknowledge"),
    EXCHANGE_REJECT("Exchange Reject");

    private final String orderStateEventDescription;

    OrderStateEvent(String orderStateEventDescription)
    {
        this.orderStateEventDescription = orderStateEventDescription;
    }

    public String getOrderStateEventDescription()
    {
        return orderStateEventDescription;
    }
}
