package com.spond.forecastservice.controller;

import com.spond.forecastservice.domain.Event;
import com.spond.forecastservice.dto.ForecastDto;
import com.spond.forecastservice.service.ForecastService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ForecastController.class)
class ForecastControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    ForecastService forecastService;


    @Test
    void when_latitudeNotSet_validationError() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/event/forecast").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(
                "Required request parameter 'latitude' for method parameter type double is not present"));
    }

    @Test
    void when_longitudeNotSet_validationError() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/event/forecast?latitude=10.1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(
                "Required request parameter 'longitude' for method parameter type double is not present"));
    }

    @Test
    void when_startTimeNotSet_validationError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/event/forecast?latitude=60.05&longitude=10.87")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(
                "Required request parameter 'startTime' for method parameter type Instant is not present"));
    }

    @Test
    void when_endTimeNotSet_validationError() throws Exception {
        Instant now = Instant.now();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/event/forecast?latitude=60.05&longitude=10.87&startTime=" + now)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(
                "Required request parameter 'endTime' for method parameter type Instant is not present"));
    }

    @Test
    void when_invalidInstantString_validationError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/event/forecast?latitude=60.05&longitude=10.87&startTime=invalidInstantString")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(
                "Method parameter 'startTime': Failed to convert value of type 'java.lang.String' to required type 'java.time.Instant'; Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.RequestParam java.time.Instant] for value [invalidInstantString]"));
    }

    @Test
    void when_eventHasEnded_ValidationError() throws Exception {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant yesterdayPlus10M = yesterday.plus(10, ChronoUnit.MINUTES);
        when(forecastService.findForecast(any(Event.class))).thenCallRealMethod();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/event/forecast?latitude=60.05&longitude=10.87&startTime=" + yesterday + "&endTime=" + yesterdayPlus10M)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(
                "Event has already ended!"));
    }

    @Test
    void when_eventStartsInMoreThan7Days_ValidationError() throws Exception {
        Instant tenDaysFromNow = Instant.now().plus(10, ChronoUnit.DAYS);
        Instant elevenDaysFromNow = tenDaysFromNow.plus(1, ChronoUnit.DAYS);
        when(forecastService.findForecast(any(Event.class))).thenCallRealMethod();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/event/forecast?latitude=60.05&longitude=10.87&startTime=" + tenDaysFromNow + "&endTime=" + elevenDaysFromNow)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(
                "Event must be at most 7 days away!"));
    }

    @Test
    void when_getSongWritersByISWCs_success() throws Exception {
        Instant now = Instant.now();
        Instant tomorrow = now.plus(1, ChronoUnit.DAYS);
        when(forecastService.findForecast(Event.builder()
            .startTime(now)
            .endTime(tomorrow)
            .latitude(60.05)
            .longitude(10.87)
            .build()))
            .thenReturn(ForecastDto.builder().airTemperature(-10.0).windSpeed(3.6).build());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/event/forecast?latitude=60.05&longitude=10.87&startTime=" + now + "&endTime=" + tomorrow)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
            .andExpect(jsonPath("$.airTemperature").value("-10.0"))
            .andExpect(jsonPath("$.windSpeed").value("3.6"));
    }
}