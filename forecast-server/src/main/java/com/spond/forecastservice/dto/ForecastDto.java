package com.spond.forecastservice.dto;

import lombok.Builder;

@Builder
public record ForecastDto(Double airTemperature, Double windSpeed) {
}
