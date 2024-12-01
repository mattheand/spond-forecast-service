package com.spond.forecastservice.service.external;

import com.github.benmanes.caffeine.cache.Cache;
import com.spond.forecastservice.externalmodel.WeatherData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
@Slf4j
public class MetApiService {

    public static final String USER_AGENT_VALUE = "spondWeatherForecast-MA/0.0.1 (oleg.topchiy@spond.teamtailor-mail.com)";
    public static final String URL_PATTERN = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%f&lon=%f";

    private final RestTemplate restTemplate;
    private final Cache<String, CachedWeatherData> weatherDataCache;

    public ResponseEntity<WeatherData> getWeatherDataResponseEntity(final double latitude, final double longitude) {
        String url = buildRequestUrl(latitude, longitude);

        // Check if data is in cache
        CachedWeatherData cachedData = weatherDataCache.getIfPresent(url);
        if (cachedData != null && cachedData.expiresAt().isAfter(Instant.now())) {
            // Return cached data if valid
            log.info("Returning data from cache!!");
            return ResponseEntity.ok(cachedData.data());
        }

        return makeApiCall(url, cachedData);
    }

    private ResponseEntity<WeatherData> makeApiCall(String url, CachedWeatherData cachedData) {
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", USER_AGENT_VALUE);
        if (cachedData != null && cachedData.lastModified() != null) {
            headers.add("If-Modified-Since", cachedData.lastModified());
        }

        ResponseEntity<WeatherData> response = restTemplate.exchange(url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            WeatherData.class);

        if (response.getStatusCode() == HttpStatus.NOT_MODIFIED && cachedData != null) {
            // Data hasn't changed, return cached data
            log.info("Not modified returned by downstream, returning data from cache!");
            return ResponseEntity.ok(cachedData.data());
        }

        cacheResponse(url, response);

        return response;
    }

    private void cacheResponse(String url, ResponseEntity<WeatherData> response) {
        log.info("Updating cache entry!!");
        HttpHeaders responseHeaders = response.getHeaders();
        String expires = responseHeaders.getFirst(HttpHeaders.EXPIRES);
        String lastModified = responseHeaders.getFirst(HttpHeaders.LAST_MODIFIED);

        weatherDataCache.put(url,
            CachedWeatherData.builder()
                .data(response.getBody())
                .expiresAt(expires != null ? toInstant(expires) : Instant.now().plus(30, ChronoUnit.SECONDS))
                .lastModified(lastModified)
                .build());
    }

    private static String buildRequestUrl(double latitude, double longitude) {
        return String.format(URL_PATTERN, latitude, longitude);
    }

    private Instant toInstant(final String date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
        return zonedDateTime.toInstant();
    }

}
