package com.project.weathermetrics.exception;

public class MetricNotFoundException extends RuntimeException {
    public MetricNotFoundException(String message) {
        super(message);
    }
}
