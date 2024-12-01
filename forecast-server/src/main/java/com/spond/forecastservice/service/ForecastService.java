package com.spond.forecastservice.service;

import com.spond.forecastservice.domain.Event;
import com.spond.forecastservice.domain.InvalidEventException;
import com.spond.forecastservice.dto.ForecastDto;
import com.spond.forecastservice.externalmodel.Data;
import com.spond.forecastservice.externalmodel.Timeseries;
import com.spond.forecastservice.externalmodel.WeatherData;
import com.spond.forecastservice.service.external.MetApiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ForecastService {

    private final MetApiService metApiService;

    public ForecastDto findForecast(final Event event) {
        validate(event);

        ResponseEntity<WeatherData> response = metApiService.getLocationForecast(event.latitude(), event.longitude());
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to retrieve forecast!");
        }
        //TODO:check for null before get
        List<Timeseries> timeseries = response.getBody().getProperties().getTimeseries();
        Data closestForecastData = findClosestForecastData(timeseries, event.startTime());

        return ForecastDto.builder()
            .windSpeed(closestForecastData.getInstant().getDetails().getWind_speed())
            .airTemperature(closestForecastData.getInstant().getDetails().getAir_temperature())
            .build();
    }

    //This method is only added to make testing easier
    protected static Instant findClosestTime(final List<Instant> times, final Instant startTime) {
        return times.get(findClosestTimeIndex(times, startTime));
    }

    private static Data findClosestForecastData(final List<Timeseries> timeseries, final Instant startTime) {
        List<Instant> times = timeseries.stream().map(Timeseries::getTime).toList();
        //Rely on the index search
        return timeseries.get(findClosestTimeIndex(times, startTime)).getData();
    }

    //Note the list is returned already sorted by timestamp, so we should not need to reorder the collection
    private static int findClosestTimeIndex(final List<Instant> times, final Instant startTime) {
        int index = Collections.binarySearch(times, startTime);

        if (index >= 0) {
            return index; // Exact match found
        }

        int insertionPoint = -(index + 1);
        int beforeIndex = insertionPoint - 1; // Closest before
        int afterIndex = insertionPoint;     // Closest after

        // Handle edge cases: startTime before the first element or after the last
        if (beforeIndex < 0) return afterIndex;
        if (afterIndex >= times.size()) return beforeIndex;

        // Calculate the difference in milliseconds and return the index of the closes value of the startTime of interest
        long beforeDiff = Math.abs(times.get(beforeIndex).toEpochMilli() - startTime.toEpochMilli());
        long afterDiff = Math.abs(times.get(afterIndex).toEpochMilli() - startTime.toEpochMilli());

        return (beforeDiff <= afterDiff) ? beforeIndex : afterIndex;
    }

    //for any event that starts in the next 7 days and has a location set
    //TODO: add validation for valid coordinates + tests
    private void validate(final Event event) {
        Instant now = Instant.now();
        // Ensure the event has not already ended
        if (event.endTime().isBefore(now)) {
            throw new InvalidEventException("Event has already ended!");
        }
        Instant sevenDaysFromNow = now.plus(7, ChronoUnit.DAYS);
        if (event.startTime().isAfter(sevenDaysFromNow)) {
            throw new InvalidEventException("Event must be at most 7 days away!");
        }
    }
}
