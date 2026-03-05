package com.project.weathermetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeatherMetricsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherMetricsApiApplication.class, args);
	}

}
