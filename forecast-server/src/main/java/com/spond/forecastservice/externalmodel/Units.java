package com.spond.forecastservice.externalmodel;

import lombok.Data;

@Data
class Units {
    private String air_pressure_at_sea_level;
    private String air_temperature;
    private String cloud_area_fraction;
    private String precipitation_amount;
    private String relative_humidity;
    private String wind_from_direction;
    private String wind_speed;
}