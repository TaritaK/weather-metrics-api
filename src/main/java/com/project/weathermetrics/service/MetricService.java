package com.project.weathermetrics.service;

import com.project.weathermetrics.dto.MetricRequest;
import com.project.weathermetrics.dto.QueryRequest;
import com.project.weathermetrics.dto.QueryResponse;
import com.project.weathermetrics.entity.Metric;
import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.exception.DateRangeException;
import com.project.weathermetrics.exception.MetricNotFoundException;
import com.project.weathermetrics.exception.SensorNotFoundException;
import com.project.weathermetrics.repository.MetricRepository;
import com.project.weathermetrics.repository.SensorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricService {
    private static final Logger logger = LoggerFactory.getLogger(MetricService.class);
    
    private final MetricRepository metricRepository;
    private final SensorRepository sensorRepository;

    private static final int MIN_RANGE_DAYS = 1;
    private static final int MAX_RANGE_DAYS = 31;

    public MetricService(MetricRepository metricRepository, SensorRepository sensorRepository) {
        this.metricRepository = metricRepository;
        this.sensorRepository = sensorRepository;
    }

    public Metric getMetricById(Long id) {
        logger.info("Fetching metric by ID: {}", id);
        return metricRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Metric not found with ID: {}", id);
                    return new MetricNotFoundException("Metric not found with ID: " + id);
                });
    }

    public Page<Metric> getAllMetrics(int page, int size) {
        long startTime = System.currentTimeMillis();
        logger.info("Fetching metrics - page: {}, size: {}", page, size);
        Page<Metric> metrics = metricRepository.findAll(PageRequest.of(page, size));
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Retrieved {} metrics in {}ms", metrics.getTotalElements(), duration);
        return metrics;
    }

    public Metric saveMetric(MetricRequest request) {
        logger.info("Saving metric for sensor ID: {}, type: {}, value: {}", 
                    request.getSensorId(), request.getMetricType(), request.getMetricValue());
        
        Sensor sensor = sensorRepository.findById(request.getSensorId())
                .orElseThrow(() -> {
                    logger.error("Sensor not found with ID: {}", request.getSensorId());
                    return new SensorNotFoundException("Sensor not found with ID: " + request.getSensorId());
                });

        Metric metric = new Metric();
        metric.setSensor(sensor);
        metric.setMetricType(request.getMetricType());
        metric.setMetricValue(request.getMetricValue());
        metric.setTimestamp(LocalDateTime.now());

        Metric savedMetric = metricRepository.save(metric);
        logger.info("Metric saved successfully with ID: {}", savedMetric.getId());
        return savedMetric;
    }

    public List<Metric> saveBatchMetrics(List<MetricRequest> requests) {
        logger.info("Batch saving {} metrics", requests.size());
        
        List<Metric> metrics = new ArrayList<>();
        for (MetricRequest request : requests) {
            Sensor sensor = sensorRepository.findById(request.getSensorId())
                    .orElseThrow(() -> new SensorNotFoundException("Sensor not found with ID: " + request.getSensorId()));

            Metric metric = new Metric();
            metric.setSensor(sensor);
            metric.setMetricType(request.getMetricType());
            metric.setMetricValue(request.getMetricValue());
            metric.setTimestamp(LocalDateTime.now());
            metrics.add(metric);
        }

        List<Metric> savedMetrics = metricRepository.saveAll(metrics);
        logger.info("Batch saved {} metrics successfully", savedMetrics.size());
        return savedMetrics;
    }

    public List<QueryResponse> queryMetrics(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("Querying metrics - Sensors: {}, Types: {}, Statistic: {}", 
                    request.getSensorIds(), request.getMetricTypes(), request.getStatistic());

        validateSensorsExist(request.getSensorIds());
        LocalDateTime[] dateRange = resolveDateRange(request.getStartDate(), request.getEndDate());
        List<QueryResponse> responses = buildQueryResponses(request, dateRange[0], dateRange[1]);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Query completed successfully, returning {} results in {}ms", responses.size(), duration);
        return responses;
    }

    private void validateSensorsExist(List<Long> sensorIds) {
        for (Long sensorId : sensorIds) {
            if (!sensorRepository.existsById(sensorId)) {
                logger.error("Sensor not found with ID: {}", sensorId);
                throw new SensorNotFoundException("Sensor not found with ID: " + sensorId);
            }
        }
    }

    private LocalDateTime[] resolveDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            end = LocalDateTime.now();
            start = end.minusDays(MIN_RANGE_DAYS);
            logger.info("Using default date range: {} to {}", start, end);
        } else {
            validateDateRange(start, end);
        }
        return new LocalDateTime[]{start, end};
    }

    private List<QueryResponse> buildQueryResponses(QueryRequest request, LocalDateTime start, LocalDateTime end) {
        List<QueryResponse> responses = new ArrayList<>();
        for (Long sensorId : request.getSensorIds()) {
            Map<String, Double> metricResults = calculateMetrics(request, sensorId, start, end);
            responses.add(new QueryResponse(sensorId, metricResults));
        }
        return responses;
    }

    private Map<String, Double> calculateMetrics(QueryRequest request, Long sensorId, LocalDateTime start, LocalDateTime end) {
        Map<String, Double> metricResults = new HashMap<>();
        for (String metricType : request.getMetricTypes()) {
            Double value = calculateStatistic(sensorId, metricType, request.getStatistic(), start, end);
            value = value != null ? value : 0.0;
            metricResults.put(metricType + "_" + request.getStatistic(), value);
        }
        return metricResults;
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            logger.warn("Invalid date range: start {} is after end {}", start, end);
            throw new DateRangeException("Start date must be before end date");
        }

        long days = ChronoUnit.DAYS.between(start, end);

        if (days < MIN_RANGE_DAYS) {
            logger.warn("Date range too short: {} days", days);
            throw new DateRangeException("Date range must be at least " + MIN_RANGE_DAYS + " day");
        }

        if (days > MAX_RANGE_DAYS) {
            logger.warn("Date range too long: {} days", days);
            throw new DateRangeException("Date range cannot exceed " + MAX_RANGE_DAYS + " days");
        }
    }

    private Double calculateStatistic(Long sensorId, String metricType, String statistic,
                                      LocalDateTime start,
                                      LocalDateTime end) {
        String stat = statistic.toLowerCase();

        return switch (stat) {
            case "avg", "average" ->
                    metricRepository.findAverageBySensorAndDateRange(sensorId, metricType, start, end);
            case "min" ->
                    metricRepository.findMinBySensorAndDateRange(sensorId, metricType, start, end);
            case "max" ->
                    metricRepository.findMaxBySensorAndDateRange(sensorId, metricType, start, end);
            case "sum" ->
                    metricRepository.findSumBySensorAndDateRange(sensorId, metricType, start, end);
            default -> {
                logger.error("Invalid statistic requested: {}", statistic);
                throw new IllegalArgumentException("Invalid statistic: " + statistic);
            }
        };
    }
}
