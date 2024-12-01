package com.spond.forecastservice.domain;

public class InvalidEventException extends RuntimeException {
    public InvalidEventException(final String message) {
        super(message);
    }
}
