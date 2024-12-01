package com.spond.forecastservice.service.external;

import com.github.benmanes.caffeine.cache.Cache;
import com.spond.forecastservice.externalmodel.WeatherData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetApiServiceTest {
    @Mock
    RestTemplate restTemplate;

    @Mock
    Cache<String, CachedWeatherData> weatherDataCache;

    @InjectMocks
    MetApiService service;


    @Test
    void when_getForecast_CacheHit_ReturnsFromCache() {
        double latitude = 10.0;
        double longitude = 20.0;

        CachedWeatherData cachedData = CachedWeatherData.builder()
            .data(weatherData())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

        when(weatherDataCache.getIfPresent(anyString())).thenReturn(cachedData);

        ResponseEntity<WeatherData> response = service.getLocationForecast(latitude, longitude);

        //assertions
        assertThat(HttpStatus.OK).isEqualTo(response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        verify(weatherDataCache, times(1)).getIfPresent(anyString());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void when_getForecast_CacheHitWithExpiredData_MakesDownstreamCall() {
        String url = mockedUrl();
        double latitude = 10.0;
        double longitude = 20.0;

        CachedWeatherData expiredData = CachedWeatherData.builder()
            .data(weatherData())
            .expiresAt(Instant.now().minusSeconds(60))
            .lastModified("Fri, 29 Nov 2024 13:34:26 GMT")
            .build();
        when(weatherDataCache.getIfPresent(url)).thenReturn(expiredData);
        when(restTemplate.exchange(eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(WeatherData.class))).thenReturn(ResponseEntity.ok(weatherData()));

        ResponseEntity<WeatherData> response = service.getLocationForecast(latitude, longitude);

        //assertions
        assertThat(HttpStatus.OK).isEqualTo(response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        verify(weatherDataCache, times(1)).getIfPresent(url);
        verify(restTemplate, times(1)).exchange(eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(WeatherData.class));
    }

    @Test
    void when_getForecast_downstreamNotModified_returnsCachedWeatherData() {
        String url = mockedUrl();
        double latitude = 10.0;
        double longitude = 20.0;

        CachedWeatherData cachedData = CachedWeatherData.builder()
            .data(weatherData())
            .expiresAt(Instant.now().minusSeconds(60))
            .lastModified("Fri, 29 Nov 2024 13:34:26 GMT")
            .build();

        when(weatherDataCache.getIfPresent(url)).thenReturn(cachedData);

        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<WeatherData> apiResponse = new ResponseEntity<>(null, headers, HttpStatus.NOT_MODIFIED);

        when(restTemplate.exchange(eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(WeatherData.class))).thenReturn(apiResponse);

        ResponseEntity<WeatherData> response = service.getLocationForecast(latitude, longitude);

        //assertions
        assertThat(HttpStatus.OK).isEqualTo(response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        verify(weatherDataCache, times(1)).getIfPresent(url);
        verify(restTemplate, times(1)).exchange(eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(WeatherData.class));
        assertThat(response.getBody()).isEqualTo(weatherData());
    }

    //TODO: add one more test where the data is modified and the cache is updated
    @Test
    void when_getForecast_downstreamModified_updatesCacheWithNewData() {

    }


    @Test
    void when_getForecast_noCacheHit_MakesCallAndUpdateCache() {
        String url = mockedUrl();
        double latitude = 10.0;
        double longitude = 20.0;

        when(weatherDataCache.getIfPresent(url)).thenReturn(null);

        ResponseEntity<WeatherData> apiResponse = ResponseEntity.ok(weatherData());
        when(restTemplate.exchange(eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(WeatherData.class))).thenReturn(apiResponse);

        ResponseEntity<WeatherData> response = service.getLocationForecast(latitude, longitude);

        //assertions
        assertThat(HttpStatus.OK).isEqualTo(response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        verify(weatherDataCache, times(1)).getIfPresent(url);
        verify(restTemplate, times(1)).exchange(eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(WeatherData.class));
        verify(weatherDataCache, times(1)).put(eq(url), any(CachedWeatherData.class));
    }

    private static String mockedUrl() {
        return "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=10.000000&lon=20.000000";
    }

    private static WeatherData weatherData() {
        return new WeatherData();
    }
}


