package com.leon.model;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;

import java.time.format.DateTimeFormatter;

public class CustomLocalTimeDeserializer extends LocalTimeDeserializer {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a");

    public CustomLocalTimeDeserializer() {
        super(FORMATTER);
    }
}