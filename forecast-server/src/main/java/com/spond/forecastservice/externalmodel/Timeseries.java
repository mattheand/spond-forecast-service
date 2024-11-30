package com.spond.forecastservice.externalmodel;

import java.time.Instant;

@lombok.Data
public class Timeseries {
    private Data data;
    private Instant time;
}