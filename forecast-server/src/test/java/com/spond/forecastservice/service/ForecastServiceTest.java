package com.spond.forecastservice.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ForecastServiceTest {
    private static List<Instant> instants;

    @BeforeAll
    static void setUp() throws IOException {
        Path filePath = Path.of("src/test/resources/compact-time-structure.txt");

        try (Stream<String> lines = Files.lines(filePath)) {
            instants = lines.map(Instant::parse).toList();
        }
    }

    @Test
    void when_findClosest_hourly_exactMatch_ExactMatchFound() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-11-28T20:00:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-11-28T20:00:00Z"));
    }

    @Test
    void when_findClosest_hourly_30mPastHour_RoundsDownToPreviousHour() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-11-28T20:30:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-11-28T20:00:00Z"));
    }

    @Test
    void when_findClosest_hourly_32PastHour_RoundsUpToNextHour() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-11-28T20:32:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-11-28T21:00:00Z"));
    }

    @Test
    void when_findClosest_hourlyTo1800_PastOneHour_FindsClosestAvailableTime() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-11-30T19:00:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-11-30T18:00:00Z"));
    }

    @Test
    void when_findClosest_hourlyTo1800_PastFourHour_FindsClosestAvailableTime() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-11-30T22:00:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-12-01T00:00:00Z"));
    }

    @Test
    void when_findClosest_every6Hours_exactMatch_FindsExactMatch() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-12-02T12:00:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-12-02T12:00:00Z"));
    }

    @Test
    void when_findClosest_every6Hours_2hourPastInterval_FindsClosestTimeBefore() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-12-02T14:00:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-12-02T12:00:00Z"));
    }

    @Test
    void when_findClosest_every6Hours_3hourPastInterval_FindsClosestTimeBefore() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-12-02T15:00:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-12-02T12:00:00Z"));
    }

    @Test
    void when_findClosest_every6Hours_3hour1MinutePastInterval_FindsClosestAfter() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-12-02T15:01:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-12-02T18:00:00Z"));
    }

    @Test
    void when_findClosest_lastDay_FindsRoundingCorrectly() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-12-08T02:00:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-12-08T00:00:00Z"));
    }

    @Test
    void when_findClosest_lastDay_PastMaxAvailable_FindsMaxAvailableTime() {
        Instant closestTime = ForecastService.findClosestTime(instants, Instant.parse("2024-12-08T10:00:00Z"));
        assertThat(closestTime).isEqualTo(Instant.parse("2024-12-08T06:00:00Z"));
    }
}