package com.leon.model;

public enum SettlementType 
{
    T_PLUS_ZERO("T+0"),
    T_PLUS_ONE("T+1"),
    T_PLUS_TWO("T+2"),
    T_PLUS_THREE("T+3");

    private final String settlementTypeName;
    SettlementType(String settlementTypeName)
    {
        this.settlementTypeName = settlementTypeName;
    }

    public String getSettlementTypeName()
    {
        return settlementTypeName;
    }
}
