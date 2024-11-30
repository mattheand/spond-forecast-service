package com.spond.forecastservice.domain;

import lombok.Builder;

import java.time.Instant;

@Builder
public record Event(double latitude, double longitude, Instant startTime, Instant endTime) {
}
