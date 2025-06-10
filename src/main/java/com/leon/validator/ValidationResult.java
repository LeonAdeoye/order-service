package com.leon.validator;

import lombok.AllArgsConstructor;
import lombok.Data;

public record ValidationResult(boolean valid, String errorMessage) {
}