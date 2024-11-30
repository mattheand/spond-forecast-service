package com.spond.forecastservice.externalmodel;

import lombok.Data;

import java.util.List;

@Data
public class Properties {
    private Meta meta;
    private List<Timeseries> timeseries;
}
