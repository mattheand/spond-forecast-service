package com.spond.forecastservice.controller;

import com.spond.forecastservice.domain.Event;
import com.spond.forecastservice.dto.ForecastDto;
import com.spond.forecastservice.service.ForecastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ForecastController {

    private final ForecastService forecastService;


    //TODO: probably makes more sense to accept an event id instead of all the parameters of the event...
    @GetMapping("/event/forecast/{eventId}")
    public ForecastDto getForecastById(@PathVariable String eventId) {
        return ForecastDto.builder().build();
    }

    @GetMapping("/event/forecast")
    public ForecastDto getForecast(@RequestParam double latitude,
                                   @RequestParam double longitude,
                                   @RequestParam Instant startTime,
                                   @RequestParam Instant endTime) {

        Event event = Event.builder()
            .latitude(latitude)
            .longitude(longitude)
            .startTime(startTime)
            .endTime(endTime)
            .build();

        return forecastService.findForecast(event);
    }
}