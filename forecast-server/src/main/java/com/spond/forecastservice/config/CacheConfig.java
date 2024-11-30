package com.spond.forecastservice.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.spond.forecastservice.service.external.CachedWeatherData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean("weatherDataCache")
    public Cache<String, CachedWeatherData> weatherDataCache() {
        return Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();
    }
}
