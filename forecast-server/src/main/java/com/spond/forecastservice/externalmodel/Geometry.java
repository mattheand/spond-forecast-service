package com.spond.forecastservice.externalmodel;

import lombok.Data;

import java.util.List;

@Data
public class Geometry {
    private List<Double> coordinates;
    private String type;
}
