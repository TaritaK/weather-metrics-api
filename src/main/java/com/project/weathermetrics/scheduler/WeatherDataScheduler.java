package com.project.weathermetrics.scheduler;

import com.project.weathermetrics.dto.MetricRequest;
import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.repository.SensorRepository;
import com.project.weathermetrics.service.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WeatherDataScheduler {
    private static final Logger logger = LoggerFactory.getLogger(WeatherDataScheduler.class);
    
    @Value("${weather.api.key}")
    private String apiKey;
    
    @Value("${weather.api.city:Dublin}")
    private String city;
    
    private final WebClient webClient;
    private final MetricService metricService;
    private final SensorRepository sensorRepository;
    private Sensor weatherSensor;

    public WeatherDataScheduler(MetricService metricService, SensorRepository sensorRepository) {
        this.metricService = metricService;
        this.sensorRepository = sensorRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openweathermap.org/data/2.5")
                .build();
        initializeSensor();
    }

    private void initializeSensor() {
        weatherSensor = sensorRepository.findAll().stream()
                .filter(s -> s.getName().equals("OpenWeather API Sensor"))
                .findFirst()
                .orElseGet(() -> {
                    Sensor sensor = new Sensor();
                    sensor.setName("OpenWeather API Sensor");
                    return sensorRepository.save(sensor);
                });
    }

    @Scheduled(fixedRate = 43200000) // every 12 hours
    //@Scheduled(fixedRate = 120000) // 2 minutes for test
    public void fetchWeatherData() {
        logger.info("Fetching weather data from OpenWeather API for {}", city);
        
        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("q", city)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null) {
                saveWeatherMetrics(response);
                logger.info("Weather data saved successfully");
            }
        } catch (Exception e) {
            logger.error("Error fetching weather data: {}", e.getMessage());
        }
    }

    private void saveWeatherMetrics(Map<String, Object> response) {
        Map<String, Object> main = (Map<String, Object>) response.get("main");
        Map<String, Object> wind = (Map<String, Object>) response.get("wind");
        LocalDateTime now = LocalDateTime.now();

        List<MetricRequest> requests = new ArrayList<>();
        requests.add(createMetricRequest("temperature", ((Number) main.get("temp")).doubleValue(), now));
        requests.add(createMetricRequest("humidity", ((Number) main.get("humidity")).doubleValue(), now));
        requests.add(createMetricRequest("pressure", ((Number) main.get("pressure")).doubleValue(), now));
        requests.add(createMetricRequest("wind-speed", ((Number) wind.get("speed")).doubleValue(), now));

        metricService.saveBatchMetrics(requests);
    }

    private MetricRequest createMetricRequest(String type, double value, LocalDateTime timestamp) {
        MetricRequest request = new MetricRequest();
        request.setSensorId(weatherSensor.getId());
        request.setMetricType(type);
        request.setMetricValue(value);
        return request;
    }
}
