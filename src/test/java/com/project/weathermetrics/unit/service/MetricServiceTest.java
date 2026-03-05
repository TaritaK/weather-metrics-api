package com.project.weathermetrics.unit.service;

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
import com.project.weathermetrics.service.MetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private MetricService metricService;

    private Sensor sensor;
    private MetricRequest metricRequest;

    @BeforeEach
    void setUp() {
        sensor = new Sensor();
        sensor.setId(1L);
        sensor.setName("Test Sensor");

        metricRequest = new MetricRequest();
        metricRequest.setSensorId(1L);
        metricRequest.setMetricType("temperature");
        metricRequest.setMetricValue(23.5);
    }

    private QueryRequest createQueryRequest(String statistic, LocalDateTime start, LocalDateTime end) {
        QueryRequest request = new QueryRequest();
        request.setSensorIds(List.of(1L));
        request.setMetricTypes(List.of("temperature"));
        request.setStatistic(statistic);
        request.setStartDate(start);
        request.setEndDate(end);
        return request;
    }

    @Test
    void getMetricById_Success() {
        Metric metric = new Metric();
        metric.setId(1L);
        metric.setSensor(sensor);
        metric.setMetricType("temperature");
        metric.setMetricValue(23.5);
        metric.setTimestamp(LocalDateTime.now());

        when(metricRepository.findById(1L)).thenReturn(Optional.of(metric));

        Metric result = metricService.getMetricById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getMetricById_NotFound() {
        when(metricRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MetricNotFoundException.class, () -> metricService.getMetricById(999L));
    }

    @Test
    void saveMetric_Success() {
        when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
        when(metricRepository.save(any(Metric.class))).thenAnswer(i -> i.getArgument(0));

        Metric result = metricService.saveMetric(metricRequest);

        assertNotNull(result);
        assertEquals("temperature", result.getMetricType());
        assertEquals(23.5, result.getMetricValue());
        verify(metricRepository).save(any(Metric.class));
    }

    @Test
    void saveMetric_SensorNotFound() {
        when(sensorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SensorNotFoundException.class, () -> metricService.saveMetric(metricRequest));
    }

    @Test
    void queryMetrics_SensorNotFound() {
        QueryRequest queryRequest = createQueryRequest("avg", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        queryRequest.setSensorIds(List.of(999L));

        when(sensorRepository.existsById(999L)).thenReturn(false);

        assertThrows(SensorNotFoundException.class, () -> metricService.queryMetrics(queryRequest));
    }

    @Test
    void queryMetrics_AvgStatistic() {
        QueryRequest queryRequest = createQueryRequest("avg", LocalDateTime.now().minusDays(7), LocalDateTime.now());

        when(sensorRepository.existsById(1L)).thenReturn(true);
        when(metricRepository.findAverageBySensorAndDateRange(any(), any(), any(), any())).thenReturn(23.5);

        List<QueryResponse> results = metricService.queryMetrics(queryRequest);

        assertEquals(23.5, results.get(0).getResults().get("temperature_avg"));
        verify(metricRepository).findAverageBySensorAndDateRange(any(), any(), any(), any());
    }
    
    @Test
    void queryMetrics_MinStatistic() {
        QueryRequest queryRequest = createQueryRequest("min", LocalDateTime.now().minusDays(7), LocalDateTime.now());

        when(sensorRepository.existsById(1L)).thenReturn(true);
        when(metricRepository.findMinBySensorAndDateRange(any(), any(), any(), any())).thenReturn(18.0);

        List<QueryResponse> results = metricService.queryMetrics(queryRequest);

        assertEquals(18.0, results.get(0).getResults().get("temperature_min"));
        verify(metricRepository).findMinBySensorAndDateRange(any(),any(), any(), any());
    }

    @Test
    void queryMetrics_MaxStatistic() {
        QueryRequest queryRequest = createQueryRequest("max", LocalDateTime.now().minusDays(7), LocalDateTime.now());

        when(sensorRepository.existsById(1L)).thenReturn(true);
        when(metricRepository.findMaxBySensorAndDateRange(any(), any(), any(), any())).thenReturn(30.0);

        List<QueryResponse> results = metricService.queryMetrics(queryRequest);

        assertEquals(30.0, results.get(0).getResults().get("temperature_max"));
        verify(metricRepository).findMaxBySensorAndDateRange(any(), any(), any(), any());
    }

    @Test
    void queryMetrics_SumStatistic() {
        QueryRequest queryRequest = createQueryRequest("sum", LocalDateTime.now().minusDays(7), LocalDateTime.now());

        when(sensorRepository.existsById(1L)).thenReturn(true);
        when(metricRepository.findSumBySensorAndDateRange(any(), any(), any(), any())).thenReturn(150.0);

        List<QueryResponse> results = metricService.queryMetrics(queryRequest);

        assertEquals(150.0, results.get(0).getResults().get("temperature_sum"));
        verify(metricRepository).findSumBySensorAndDateRange(any(), any(), any(), any());
    }

    @Test
    void queryMetrics_InvalidStatistic() {
        QueryRequest queryRequest = createQueryRequest("invalid", LocalDateTime.now().minusDays(7), LocalDateTime.now());

        when(sensorRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> metricService.queryMetrics(queryRequest));
    }
    
    @Test
    void queryMetrics_DefaultDateRange() {
        QueryRequest queryRequest = createQueryRequest("avg", null, null);

        when(sensorRepository.existsById(1L)).thenReturn(true);
        when(metricRepository.findAverageBySensorAndDateRange(any(), any(), any(), any())).thenReturn(23.5);

        List<QueryResponse> results = metricService.queryMetrics(queryRequest);

        assertEquals(23.5, results.get(0).getResults().get("temperature_avg"));
        verify(metricRepository).findAverageBySensorAndDateRange(any(), any(), any(), any());
    }

    @Test
    void queryMetrics_StartDateAfterEndDate() {
        QueryRequest queryRequest = createQueryRequest("avg", LocalDateTime.now(), LocalDateTime.now().minusDays(7));

        when(sensorRepository.existsById(1L)).thenReturn(true);

        assertThrows(DateRangeException.class, () -> metricService.queryMetrics(queryRequest));
    }
    
    @Test
    void queryMetrics_DateRangeTooShort() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 28, 12, 0);
        QueryRequest queryRequest = createQueryRequest("avg", now, now.plusHours(12));

        when(sensorRepository.existsById(1L)).thenReturn(true);

        assertThrows(DateRangeException.class, () -> metricService.queryMetrics(queryRequest));
    }

    @Test
    void queryMetrics_DateRangeTooLong() {
        QueryRequest queryRequest = createQueryRequest("avg", LocalDateTime.now().minusDays(40), LocalDateTime.now());

        when(sensorRepository.existsById(1L)).thenReturn(true);

        assertThrows(DateRangeException.class, () -> metricService.queryMetrics(queryRequest));
    }
}
