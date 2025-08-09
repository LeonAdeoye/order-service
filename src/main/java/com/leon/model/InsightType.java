package com.leon.model;

public enum InsightType
{
    CLIENT,
    SECTOR,
    COUNTRY,
    INSTRUMENT,
    UNKNOWN;

    public static InsightType fromString(String value)
    {
        if (value == null || value.isBlank()) return UNKNOWN;
        return switch (value.trim().toLowerCase())
        {
            case "client" -> CLIENT;
            case "sector" -> SECTOR;
            case "country" -> COUNTRY;
            case "instrument" -> INSTRUMENT;
            default -> UNKNOWN;
        };
    }
}


