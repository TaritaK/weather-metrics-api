package com.project.weathermetrics.service;

import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.exception.SensorNotFoundException;
import com.project.weathermetrics.repository.SensorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorService {
    private static final Logger logger = LoggerFactory.getLogger(SensorService.class);
    
    private final SensorRepository sensorRepository;

    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    public Sensor createSensor(Sensor sensor) {
        logger.info("Creating sensor: {}", sensor.getName());
        Sensor saved = sensorRepository.save(sensor);
        logger.info("Sensor created with ID: {}", saved.getId());
        return saved;
    }

    public List<Sensor> getAllSensors() {
        logger.info("Fetching all sensors");
        return sensorRepository.findAll();
    }

    public Sensor getSensorById(Long id) {
        logger.info("Fetching sensor by ID: {}", id);
        return sensorRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Sensor not found with ID: {}", id);
                    return new SensorNotFoundException("Sensor not found with ID: " + id);
                });
    }
}
