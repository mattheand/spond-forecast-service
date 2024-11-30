package com.spond.forecastservice.externalmodel;

import lombok.Data;

@Data
public class WeatherData {
    private Geometry geometry;
    private Properties properties;
    private String type;
}