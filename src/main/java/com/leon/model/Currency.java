package com.leon.model;

public enum Currency
{
    USD("US Dollar"),
    EUR("Euro"),
    GBP("British Pound"),
    JPY("Japanese Yen"),
    AUD("Australian Dollar"),
    CAD("Canadian Dollar"),
    CHF("Swiss Franc"),
    HKD("Hong Kong Dollar"),
    SGD("Singapore Dollar"),
    NOK("Norwegian Krone"),
    KRW("South Korean Won"),
    INR("Indian Rupee"),
    RUB("Russian Ruble"),
    ZAR("South African Rand"),
    MXN("Mexican Peso"),
    BRL("Brazilian Real"),
    AED("United Arab Emirates Dirham"),
    PLN("Polish Zloty"),
    TRY("Turkish Lira"),
    IDR("Indonesian Rupiah"),
    THB("Thai Baht"),
    MYR("Malaysian Ringgit"),
    PHP("Philippine Peso"),
    VND("Vietnamese Dong"),
    ARS("Argentine Peso"),
    CLP("Chilean Peso"),
    COP("Colombian Peso"),
    EGP("Egyptian Pound"),
    CNY("Chinese Yuan"),
    SEK("Swedish Krona"),
    NZD("New Zealand Dollar");

    private final String currencyName;

    Currency(String currencyName)
    {
        this.currencyName = currencyName;
    }

    public String getCurrencyName()
    {
        return currencyName;
    }
}
