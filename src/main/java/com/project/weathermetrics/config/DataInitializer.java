package com.project.weathermetrics.config;

import com.project.weathermetrics.entity.Metric;
import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.repository.MetricRepository;
import com.project.weathermetrics.repository.SensorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.Random;

@Configuration
@Profile("dev")
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(SensorRepository sensorRepository, MetricRepository metricRepository) {
        return args -> {
            if (sensorRepository.count() > 0) {
                logger.info("Database already contains data, skipping initialization");
                return;
            }

            logger.info("Initializing database with sample data...");

            Sensor sensor1 = new Sensor();
            sensor1.setName("Sensor_1");
            sensor1 = sensorRepository.save(sensor1);

            Sensor sensor2 = new Sensor();
            sensor2.setName("Sensor_2");
            sensor2 = sensorRepository.save(sensor2);

            Random random = new Random();
            LocalDateTime now = LocalDateTime.now();

            for (int day = 0; day < 7; day++) {
                for (int hour = 0; hour < 24; hour += 6) {
                    LocalDateTime timestamp = now.minusDays(day).minusHours(hour);


                    createMetric(metricRepository, sensor1, "temperature", 18 + random.nextDouble() * 10, timestamp);
                    createMetric(metricRepository, sensor1, "humidity", 50 + random.nextDouble() * 30, timestamp);

                    // Sensor 2 metrics
                    createMetric(metricRepository, sensor2, "temperature", 15 + random.nextDouble() * 12, timestamp);
                    createMetric(metricRepository, sensor2, "humidity", 45 + random.nextDouble() * 35, timestamp);
                }
            }

            logger.info("Database initialized with {} sensors and {} metrics",
                    sensorRepository.count(), metricRepository.count());
        };
    }

    private void createMetric(MetricRepository repository, Sensor sensor, String type, double value, LocalDateTime timestamp) {
        Metric metric = new Metric();
        metric.setSensor(sensor);
        metric.setMetricType(type);
        metric.setMetricValue(value);
        metric.setTimestamp(timestamp);
        repository.save(metric);
    }
}
