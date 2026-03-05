package com.project.weathermetrics.unit.repository;

import com.project.weathermetrics.entity.Metric;
import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.repository.MetricRepository;
import com.project.weathermetrics.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MetricRepositoryTest {

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private SensorRepository sensorRepository;

    private Sensor sensor;
    private LocalDateTime now;
    private static final String TEST_METRIC_TYPE = "temperature";

    @BeforeEach
    void setUp() {
        metricRepository.deleteAll();
        sensorRepository.deleteAll();

        sensor = new Sensor();
        sensor.setName("Test Sensor");
        sensor = sensorRepository.save(sensor);

        now = LocalDateTime.now();

        createMetric(20.0, now.minusDays(5));
        createMetric(25.0, now.minusDays(3));
        createMetric(30.0, now.minusDays(1));
    }

    private void createMetric(double value, LocalDateTime timestamp) {
        Metric metric = new Metric();
        metric.setSensor(sensor);
        metric.setMetricType(TEST_METRIC_TYPE);
        metric.setMetricValue(value);
        metric.setTimestamp(timestamp);
        metricRepository.save(metric);
    }

    @Test
    void findBySensorIdAndTimestampBetween() {
        List<Metric> metrics = metricRepository.findBySensorIdAndTimestampBetween(
                sensor.getId(),
                now.minusDays(6),
                now
        );

        assertEquals(3, metrics.size());
    }

    @Test
    void findAverageBySensorAndDateRange() {
        Double avg = metricRepository.findAverageBySensorAndDateRange(
                sensor.getId(),
                TEST_METRIC_TYPE,
                now.minusDays(6),
                now
        );

        assertEquals(25.0, avg);
    }

    @Test
    void findMinBySensorAndDateRange() {
        Double min = metricRepository.findMinBySensorAndDateRange(
                sensor.getId(),
                TEST_METRIC_TYPE,
                now.minusDays(6),
                now
        );

        assertEquals(20.0, min);
    }

    @Test
    void findMaxBySensorAndDateRange() {
        Double max = metricRepository.findMaxBySensorAndDateRange(
                sensor.getId(),
                TEST_METRIC_TYPE,
                now.minusDays(6),
                now
        );

        assertEquals(30.0, max);
    }
}
