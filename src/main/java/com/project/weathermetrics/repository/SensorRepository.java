package com.project.weathermetrics.repository;

import com.project.weathermetrics.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  SensorRepository extends JpaRepository<Sensor, Long> {
}
