package com.leon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderFiniteStateMachineService
{
    @Autowired
    private CurrencyService currencyService;
    public void newOrder()
    {
        // This method would typically be used to initialize a new order state machine
        // For example, setting up the initial state, validating inputs, etc.
        // Here we can just log or perform any necessary setup.
        System.out.println("New order state machine initialized.");
    }

    public void processOrder(String orderId)
    {
        // This method would typically handle the processing of an order
        // For example, transitioning states, validating conditions, etc.
        // Here we can just log or perform any necessary processing.
        System.out.println("Processing order with ID: " + orderId);
    }

}
