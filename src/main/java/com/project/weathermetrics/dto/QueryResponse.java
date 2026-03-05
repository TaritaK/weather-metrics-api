package com.project.weathermetrics.dto;

import java.util.Map;

public class QueryResponse {
    private Long sensorId;
    private Map<String, Double> results;

    public QueryResponse() {
    }

    public QueryResponse(Long sensorId, Map<String, Double> results) {
        this.sensorId = sensorId;
        this.results = results;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public Map<String, Double> getResults() {
        return results;
    }

    public void setResults(Map<String, Double> results) {
        this.results = results;
    }
}
