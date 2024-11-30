package com.spond.forecastservice.externalmodel;

import lombok.Data;

@Data
public class Details {
    private Double air_pressure_at_sea_level;
    private Double air_temperature;
    private Double cloud_area_fraction;
    private Double relative_humidity;
    private Double wind_from_direction;
    private Double wind_speed;
}
