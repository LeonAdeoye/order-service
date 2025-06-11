package com.leon.validator;

import org.springframework.stereotype.Component;

@Component
public class OrderMessageValidator {
    public ValidationResult validateMessage(String data)
    {
        return new ValidationResult(true, "");
    }
}