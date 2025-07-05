package com.leon.service;

public interface CurrencyService {
    void loadExchangeRates();
    double getExchangeRate(String currency);
}
