package com.project.weathermetrics.controller;

import com.project.weathermetrics.dto.MetricRequest;
import com.project.weathermetrics.dto.QueryRequest;
import com.project.weathermetrics.dto.QueryResponse;
import com.project.weathermetrics.entity.Metric;
import com.project.weathermetrics.service.MetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics", description = "API for managing weather metrics from sensors")
public class MetricController {
    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @Operation(summary = "Get metric by ID", description = "Get a specific metric by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Metric found"),
        @ApiResponse(responseCode = "404", description = "Metric not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Metric> getMetricById(
            @Parameter(description = "Metric ID") @PathVariable Long id) {
        Metric metric = metricService.getMetricById(id);
        return ResponseEntity.ok(metric);
    }

    @Operation(summary = "Get all metrics", description = "Get all metrics from all sensors")
    @GetMapping
    public ResponseEntity<List<Metric>> getAllMetrics() {
        List<Metric> metrics = metricService.getAllMetrics();
        return ResponseEntity.ok(metrics);
    }

    @Operation(summary = "Create a new metric", description = "Save a new metric reading from a sensor")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Metric created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    @PostMapping
    public ResponseEntity<Metric> createMetric(@Valid @RequestBody MetricRequest request) {
        Metric metric = metricService.saveMetric(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(metric);
    }

    @Operation(summary = "Query metrics with aggregations", 
               description = "Returns aggregated statistics (avg, min, max, sum) for specified sensorIds and metric types for  a date range")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved data"),
        @ApiResponse(responseCode = "400", description = "Invalid date range or parameters"),
        @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    @PostMapping("/query")
    public ResponseEntity<List<QueryResponse>> queryMetrics(@Valid @RequestBody QueryRequest request) {
        List<QueryResponse> results = metricService.queryMetrics(request);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Query metrics via GET", 
               description = "GET endpoint for querying metrics with query parameters")
    @GetMapping("/query")
    public ResponseEntity<List<QueryResponse>> queryMetricsGet(
            @Parameter(description = "Sensor IDs", example = "1,2,3") @RequestParam List<Long> sensorIds,
            @Parameter(description = "Metric types", example = "temperature,humidity") @RequestParam List<String> metricTypes,
            @Parameter(description = "Statistical operation", example = "avg") @RequestParam String statistic,
            @Parameter(description = "Start date (ISO format)", example = "2026-03-01T00:00:00") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (ISO format)", example = "2026-03-03T23:59:59") @RequestParam(required = false) String endDate
    ) {
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

        QueryRequest request = new QueryRequest();
        request.setSensorIds(sensorIds);
        request.setMetricTypes(metricTypes);
        request.setStatistic(statistic);
        request.setStartDate(start);
        request.setEndDate(end);

        List<QueryResponse> results = metricService.queryMetrics(request);
        return ResponseEntity.ok(results);
    }
}
