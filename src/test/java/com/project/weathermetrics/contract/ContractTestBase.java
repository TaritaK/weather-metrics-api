package com.project.weathermetrics.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.weathermetrics.controller.MetricController;
import com.project.weathermetrics.controller.SensorController;
import com.project.weathermetrics.dto.QueryRequest;
import com.project.weathermetrics.dto.QueryResponse;
import com.project.weathermetrics.entity.Metric;
import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.service.MetricService;
import com.project.weathermetrics.service.SensorService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {SensorController.class, MetricController.class})
public abstract class ContractTestBase {

    @Autowired
    private SensorController sensorController;

    @Autowired
    private MetricController metricController;

    @MockBean
    private SensorService sensorService;

    @MockBean
    private MetricService metricService;

    @BeforeEach
    public void setup() {

        Sensor sensor = new Sensor();
        sensor.setId(1L);
        sensor.setName("Temperature Sensor");

        when(sensorService.getAllSensors(anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of(sensor), PageRequest.of(0, 10), 1));
        when(sensorService.createSensor(any(Sensor.class))).thenReturn(sensor);
        when(sensorService.getSensorById(1L)).thenReturn(sensor);
        
        Metric metric = new Metric();
        metric.setId(1L);
        metric.setSensor(sensor);
        metric.setMetricType("temperature");
        metric.setMetricValue(23.5);
        metric.setTimestamp(LocalDateTime.parse("2026-02-28T10:00:00"));

        when(metricService.saveMetric(any())).thenReturn(metric);
        when(metricService.getAllMetrics(anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of(metric), PageRequest.of(0, 10), 1));

        QueryResponse queryResponse = new QueryResponse(1L, Map.of("temperature_avg", 23.5));
        when(metricService.queryMetrics(any(QueryRequest.class))).thenReturn(List.of(queryResponse));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders
                .standaloneSetup(sensorController, metricController)
                .setMessageConverters(converter);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }
}

