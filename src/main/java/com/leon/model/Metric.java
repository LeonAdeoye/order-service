package com.leon.model;

public enum Metric {
    SHARES,
    NOTIONAL_USD,
    NOTIONAL_LOCAL;

    public static Metric fromString(String value)
    {
        if (value == null || value.isBlank()) return SHARES;
        return switch (value.trim())
        {
            case "notionalUSD" -> NOTIONAL_USD;
            case "notionalLocal" -> NOTIONAL_LOCAL;
            default -> SHARES;
        };
    }
}
