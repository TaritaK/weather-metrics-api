package com.project.weathermetrics.integration.scheduler;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.project.weathermetrics.entity.Metric;
import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.repository.MetricRepository;
import com.project.weathermetrics.repository.SensorRepository;
import com.project.weathermetrics.scheduler.WeatherDataScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "weather.api.key=test-api-key",
        "weather.api.city=Dublin, IE",
        "weather.api.url=http://localhost:8089"
})
class WeatherDataSchedulerIntegrationTest {

    @Autowired
    private WeatherDataScheduler weatherDataScheduler;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private SensorRepository sensorRepository;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        metricRepository.deleteAll();
        
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void fetchWeatherData_Success() {
        String mockResponse = """
                {
                    "main": {
                        "temp": 15.5,
                        "humidity": 70,
                        "pressure": 1013
                    },
                    "wind": {
                        "speed": 5.2
                    }
                }
                """;

        stubFor(get(urlPathEqualTo("/weather"))
                .withQueryParam("q", equalTo("Dublin, IE"))
                .withQueryParam("appid", equalTo("test-api-key"))
                .withQueryParam("units", equalTo("metric"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        weatherDataScheduler.fetchWeatherData();

        List<Metric> metrics = metricRepository.findAll();
        assertEquals(4, metrics.size());

        assertTrue(metrics.stream().anyMatch(m -> m.getMetricType().equals("temperature") && m.getMetricValue() == 15.5));
        assertTrue(metrics.stream().anyMatch(m -> m.getMetricType().equals("humidity") && m.getMetricValue() == 70));
        assertTrue(metrics.stream().anyMatch(m -> m.getMetricType().equals("pressure") && m.getMetricValue() == 1013));
        assertTrue(metrics.stream().anyMatch(m -> m.getMetricType().equals("wind-speed") && m.getMetricValue() == 5.2));
    }

    @Test
    void fetchWeatherData_ApiError() {
        stubFor(get(urlPathEqualTo("/weather"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        long initialCount = metricRepository.count();
        
        weatherDataScheduler.fetchWeatherData();

        assertEquals(initialCount, metricRepository.count());
    }

    @Test
    void fetchWeatherData_Timeout() {
        stubFor(get(urlPathEqualTo("/weather"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(35000)));

        long initialCount = metricRepository.count();
        
        weatherDataScheduler.fetchWeatherData();

        assertEquals(initialCount, metricRepository.count());
    }

    @Test
    void fetchWeatherData_CreatesSensor() {
        String mockResponse = """
                {
                    "main": {
                        "temp": 20.0,
                        "humidity": 65,
                        "pressure": 1015
                    },
                    "wind": {
                        "speed": 3.5
                    }
                }
                """;

        stubFor(get(urlPathEqualTo("/weather"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        weatherDataScheduler.fetchWeatherData();

        Sensor sensor = sensorRepository.findAll().stream()
                .filter(s -> s.getName().equals("OpenWeather API Sensor"))
                .findFirst()
                .orElse(null);

        assertNotNull(sensor);
        
        List<Metric> metrics = metricRepository.findAll();
        assertTrue(metrics.stream().allMatch(m -> m.getSensor().getId().equals(sensor.getId())));
    }
}
