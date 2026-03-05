package com.project.weathermetrics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Request for querying metric statistics over a date range")
public class QueryRequest {
    
    @Schema(description = "List of sensor IDs to query", example = "[1, 2, 3]", required = true)
    @NotEmpty(message = "At least one sensor ID must be provided")
    private List<Long> sensorIds;

    @Schema(description = "Metric types to aggregate", example = "[\"temperature\", \"humidity\"]", required = true)
    @NotEmpty(message = "At least one metric type must be provided")
    private List<String> metricTypes;

    @Schema(description = "Statistical operation to perform", example = "avg", allowableValues = {"avg", "average", "min", "max", "sum"}, required = true)
    @NotEmpty(message = "Statistic type must be provided (MIN, MAX, AVG, SUM")
    private String statistic;

    @Schema(description = "Start date for the query range. Defaults to 1 day ago if not given", example = "2026-03-01T00:00:00")
    private LocalDateTime startDate;
    
    @Schema(description = "End date for the query range. Defaults to now if not given", example = "2026-03-03T23:59:59")
    private LocalDateTime endDate;

    public List<Long> getSensorIds() {
        return sensorIds;
    }
    public void setSensorIds(List<Long> sensorIds) {
        this.sensorIds = sensorIds;
    }

    public List<String> getMetricTypes() {
        return metricTypes;
    }
    public void setMetricTypes(List<String> metricTypes) {
        this.metricTypes = metricTypes;
    }

    public String getStatistic() {
        return statistic;
    }
    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
