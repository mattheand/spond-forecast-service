package com.spond.forecastservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpondForecastServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpondForecastServiceApplication.class, args);
	}

}
