package com.leon.validator;

import org.springframework.stereotype.Component;

@Component
public class OrderMessageValidator {
    public ValidationResult validateMessage(String data)
    {
        // TODO: Implement validation logic
        return new ValidationResult(true, "");
    }
}