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

        //TODO handle response codes properly, check for 200 for example...
        ResponseEntity<WeatherData> response = metApiService.getWeatherDataResponseEntity(event.latitude(),
            event.longitude());
        WeatherData weatherData = response.getBody();
        if (weatherData == null) {
            throw new RuntimeException("Failed to retrieve forecast weatherData.");
        }

        List<Timeseries> timeseries = weatherData.getProperties().getTimeseries();
        Data closestForecastData = findClosestForecastData(timeseries, event.startTime());

        return ForecastDto.builder()
            .windSpeed(closestForecastData.getInstant().getDetails().getWind_speed())
            .airTemperature(closestForecastData.getInstant().getDetails().getAir_temperature())
            .build();
    }


    public static Instant findClosestTime(final List<Instant> times, final Instant startTime) {
        return times.get(findClosestTimeIndex(times, startTime));
    }

    public static Data findClosestForecastData(final List<Timeseries> timeseries, final Instant startTime) {
        List<Instant> times = timeseries.stream().map(Timeseries::getTime).toList();
        //Rely on the index search
        return timeseries.get(findClosestTimeIndex(times, startTime)).getData();
    }

    //Todo: create an utils class and move this there
    public static int findClosestTimeIndex(final List<Instant> times, final Instant startTime) {
        int index = Collections.binarySearch(times, startTime);

        if (index >= 0) {
            // Exact match found
            return index;
        }

        int insertionPoint = -(index + 1);

        // Get the indices of the closest times (before and after)
        int beforeIndex = (insertionPoint > 0) ? insertionPoint - 1 : -1;
        int afterIndex = (insertionPoint < times.size()) ? insertionPoint : -1;

        // Return the closest of the two (before and after)
        if (beforeIndex == -1)
            return afterIndex;
        if (afterIndex == -1)
            return beforeIndex;

        // Calculate the difference in milliseconds and return the closest index
        long beforeDiff = Math.abs(times.get(beforeIndex).toEpochMilli() - startTime.toEpochMilli());
        long afterDiff = Math.abs(times.get(afterIndex).toEpochMilli() - startTime.toEpochMilli());

        return beforeDiff <= afterDiff ? beforeIndex : afterIndex;
    }

    //for any event that starts in the next 7 days and has a location set
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
