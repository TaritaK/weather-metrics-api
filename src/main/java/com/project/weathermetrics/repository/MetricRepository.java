package com.project.weathermetrics.repository;

import com.project.weathermetrics.entity.Metric;
import com.project.weathermetrics.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {



    List<Metric> findBySensorAndTimestampBetween(Sensor sensor, LocalDateTime start, LocalDateTime end);

    @Query("SELECT m FROM Metric m WHERE m.sensor.id = :sensorId AND m.timestamp BETWEEN :start AND :end")
    List<Metric> findBySensorIdAndTimestampBetween(@Param("sensorId") Long sensorId,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(AVG(m.metricValue), 0) FROM Metric m WHERE m.sensor.id = :sensorId AND m.metricType = :metricType AND m.timestamp BETWEEN :start AND :end")
    Double findAverageBySensorAndDateRange(@Param("sensorId") Long sensorId,
                                           @Param("metricType") String metricType,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(MIN(m.metricValue), 0) FROM Metric m WHERE m.sensor.id = :sensorId AND m.metricType = :metricType AND m.timestamp BETWEEN :start AND :end")
    Double findMinBySensorAndDateRange(@Param("sensorId") Long sensorId,
                                       @Param("metricType") String metricType,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(MAX(m.metricValue), 0) FROM Metric m WHERE m.sensor.id = :sensorId AND m.metricType = :metricType AND m.timestamp BETWEEN :start AND :end")
    Double findMaxBySensorAndDateRange(@Param("sensorId") Long sensorId,
                                       @Param("metricType") String metricType,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(m.metricValue), 0) FROM Metric m WHERE m.sensor.id = :sensorId AND m.metricType = :metricType AND m.timestamp BETWEEN :start AND :end")
    Double findSumBySensorAndDateRange(@Param("sensorId") Long sensorId,
                                       @Param("metricType") String metricType,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
