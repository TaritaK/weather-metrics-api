package com.project.weathermetrics.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.weathermetrics.dto.MetricRequest;
import com.project.weathermetrics.dto.QueryRequest;
import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.repository.MetricRepository;
import com.project.weathermetrics.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MetricControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private MetricRepository metricRepository;

    private Sensor sensor;

    @BeforeEach
    void setUp() {
        metricRepository.deleteAll();
        sensorRepository.deleteAll();

        sensor = new Sensor();
        sensor.setName("Test Sensor");
        sensor = sensorRepository.save(sensor);
    }

    @Test
    void createMetric_Success() throws Exception {
        MetricRequest request = new MetricRequest();
        request.setSensorId(sensor.getId());
        request.setMetricType("temperature");
        request.setMetricValue(23.5);

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metricType").value("temperature"))
                .andExpect(jsonPath("$.metricValue").value(23.5));
    }

    @Test
    void createMetric_ValidationError() throws Exception {
        MetricRequest request = new MetricRequest();

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void queryMetrics_Success() throws Exception {

        MetricRequest metricRequest = new MetricRequest();
        metricRequest.setSensorId(sensor.getId());
        metricRequest.setMetricType("temperature");
        metricRequest.setMetricValue(23.5);

        mockMvc.perform(post("/api/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricRequest)));


        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId()));
        queryRequest.setMetricTypes(List.of("temperature"));
        queryRequest.setStatistic("avg");
        queryRequest.setStartDate(LocalDateTime.now().minusDays(1));
        queryRequest.setEndDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorId").value(sensor.getId()))
                .andExpect(jsonPath("$[0].results.temperature_avg").value(23.5));
    }

    @Test
    void queryMetrics_InvalidDateRange() throws Exception {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId()));
        queryRequest.setMetricTypes(List.of("temperature"));
        queryRequest.setStatistic("avg");
        queryRequest.setStartDate(LocalDateTime.now().minusDays(40));
        queryRequest.setEndDate(LocalDateTime.now());

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllMetrics_Success() throws Exception {
        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void createMetric_SensorNotFound() throws Exception {
        MetricRequest request = new MetricRequest();
        request.setSensorId(99999L);
        request.setMetricType("temperature");
        request.setMetricValue(23.5);

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void queryMetrics_MinStatistic() throws Exception {
        MetricRequest metricRequest = new MetricRequest();
        metricRequest.setSensorId(sensor.getId());
        metricRequest.setMetricType("temperature");
        metricRequest.setMetricValue(20.0);

        mockMvc.perform(post("/api/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricRequest)));

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId()));
        queryRequest.setMetricTypes(List.of("temperature"));
        queryRequest.setStatistic("min");
        queryRequest.setStartDate(LocalDateTime.now().minusDays(1));
        queryRequest.setEndDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].results.temperature_min").value(20.0));
    }

    @Test
    void queryMetrics_MaxStatistic() throws Exception {
        MetricRequest metricRequest = new MetricRequest();
        metricRequest.setSensorId(sensor.getId());
        metricRequest.setMetricType("temperature");
        metricRequest.setMetricValue(30.0);

        mockMvc.perform(post("/api/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricRequest)));

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId()));
        queryRequest.setMetricTypes(List.of("temperature"));
        queryRequest.setStatistic("max");
        queryRequest.setStartDate(LocalDateTime.now().minusDays(1));
        queryRequest.setEndDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].results.temperature_max").value(30.0));
    }

    @Test
    void queryMetrics_SumStatistic() throws Exception {
        MetricRequest metricRequest = new MetricRequest();
        metricRequest.setSensorId(sensor.getId());
        metricRequest.setMetricType("temperature");
        metricRequest.setMetricValue(15.0);

        mockMvc.perform(post("/api/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricRequest)));

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId()));
        queryRequest.setMetricTypes(List.of("temperature"));
        queryRequest.setStatistic("sum");
        queryRequest.setStartDate(LocalDateTime.now().minusDays(1));
        queryRequest.setEndDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].results.temperature_sum").value(15.0));
    }

    @Test
    void queryMetrics_InvalidStatistic() throws Exception {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId()));
        queryRequest.setMetricTypes(List.of("temperature"));
        queryRequest.setStatistic("invalid");
        queryRequest.setStartDate(LocalDateTime.now().minusDays(1));
        queryRequest.setEndDate(LocalDateTime.now());

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void queryMetrics_DateRangeTooShort() throws Exception {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId()));
        queryRequest.setMetricTypes(List.of("temperature"));
        queryRequest.setStatistic("avg");
        queryRequest.setStartDate(LocalDateTime.now().minusHours(12));
        queryRequest.setEndDate(LocalDateTime.now());

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void queryMetrics_MultipleSensors() throws Exception {
        Sensor sensor2 = new Sensor();
        sensor2.setName("Test Sensor 2");
        sensor2 = sensorRepository.save(sensor2);

        MetricRequest metricRequest1 = new MetricRequest();
        metricRequest1.setSensorId(sensor.getId());
        metricRequest1.setMetricType("temperature");
        metricRequest1.setMetricValue(20.0);

        mockMvc.perform(post("/api/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricRequest1)));

        MetricRequest metricRequest2 = new MetricRequest();
        metricRequest2.setSensorId(sensor2.getId());
        metricRequest2.setMetricType("temperature");
        metricRequest2.setMetricValue(25.0);

        mockMvc.perform(post("/api/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricRequest2)));

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId(), sensor2.getId()));
        queryRequest.setMetricTypes(List.of("temperature"));
        queryRequest.setStatistic("avg");
        queryRequest.setStartDate(LocalDateTime.now().minusDays(1));
        queryRequest.setEndDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorId").exists())
                .andExpect(jsonPath("$[1].sensorId").exists());
    }

    @Test
    void queryMetrics_MultipleMetricTypes() throws Exception {
        MetricRequest tempRequest = new MetricRequest();
        tempRequest.setSensorId(sensor.getId());
        tempRequest.setMetricType("temperature");
        tempRequest.setMetricValue(22.0);

        mockMvc.perform(post("/api/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tempRequest)));

        MetricRequest humidityRequest = new MetricRequest();
        humidityRequest.setSensorId(sensor.getId());
        humidityRequest.setMetricType("humidity");
        humidityRequest.setMetricValue(65.0);

        mockMvc.perform(post("/api/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(humidityRequest)));

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSensorIds(List.of(sensor.getId()));
        queryRequest.setMetricTypes(List.of("temperature", "humidity"));
        queryRequest.setStatistic("avg");
        queryRequest.setStartDate(LocalDateTime.now().minusDays(1));
        queryRequest.setEndDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/metrics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].results.temperature_avg").value(22.0))
                .andExpect(jsonPath("$[0].results.humidity_avg").value(65.0));
    }

    @Test
    void getAllMetrics_WithPagination() throws Exception {
        mockMvc.perform(get("/api/metrics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }
}
