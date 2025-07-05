package com.leon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyServiceImpl implements CurrencyService
{
    private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);
    private static final String FX_URL = "https://openexchangerates.org/api/latest.json?app_id=0d1601b10ca0490b960214675c968c6f";
    private final Map<String, Double> exchangeRates = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadExchangeRates()
    {
        try
        {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(FX_URL, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode rates = root.get("rates");
            rates.fieldNames().forEachRemaining(code -> exchangeRates.put(code, rates.get(code).asDouble()));
            logger.info("Exchange rates loaded.");
        }
        catch (Exception e)
        {
            logger.error("Failed to load exchange rates", e);
        }
    }

    public double getExchangeRate(String currency)
    {
        return exchangeRates.getOrDefault(currency, 1.0);
    }
} 