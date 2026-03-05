package com.project.weathermetrics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Schema(description = "Request for creating a new metric reading")
public class MetricRequest {
    
    @Schema(description = "ID of the sensor submitting this metric", example = "1", required = true)
    @NotNull(message = "Sensor ID cannot be null")
    private Long sensorId;

    @Schema(description = "Metric type being submitted", example = "temperature", required = true)
    @NotBlank(message = "Metric type cannot be null or blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,50}$", message = "Invalid metric type format")
    private String metricType;

    @Schema(description = "Value of the metric", example = "23.5", required = true)
    @Positive(message = "Metric value cannot be null")
    private Double metricValue;

    public Long getSensorId() {
        return sensorId;
    }
    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

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
}
