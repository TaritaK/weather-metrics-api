package com.project.weathermetrics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics")
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "sensor_id")  // FK column to Sensor table
    private Sensor sensor;

    private String metricType;
    private Double metricValue;
    private LocalDateTime timestamp;

    public Metric() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Sensor getSensor() { return sensor; }
    public void setSensor(Sensor sensor) { this.sensor = sensor; }

    public String getMetricType() {
        return metricType;
    }
    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public Double getMetricValue() {
        return metricValue;
    }
    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}