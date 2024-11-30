package com.spond.forecastservice.service.external;

import com.spond.forecastservice.externalmodel.WeatherData;
import lombok.Builder;

import java.time.Instant;

@Builder
public record CachedWeatherData(WeatherData data, Instant expiresAt, String lastModified) {
}
