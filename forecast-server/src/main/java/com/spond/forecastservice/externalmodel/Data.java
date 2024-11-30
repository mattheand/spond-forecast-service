package com.spond.forecastservice.externalmodel;

@lombok.Data
public class Data {
    private Instant instant;
    private Next next_12_hours;
    private Next next_1_hours;
    private Next next_6_hours;
}
