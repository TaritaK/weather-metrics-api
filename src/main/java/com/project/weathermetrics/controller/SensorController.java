package com.project.weathermetrics.controller;

import com.project.weathermetrics.entity.Sensor;
import com.project.weathermetrics.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@Tag(name = "Sensors", description = "API for managing weather sensors")
public class SensorController {
    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @Operation(summary = "Create a new sensor", description = "Add a new weather sensor")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sensor created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Sensor> createSensor(@Valid @RequestBody Sensor sensor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorService.createSensor(sensor));
    }

    @Operation(summary = "Get all sensors", description = "Get a list of all sensors in the system")
    @GetMapping
    public ResponseEntity<List<Sensor>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    @Operation(summary = "Get sensor by ID", description = "Get a specific sensor by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sensor found"),
        @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Sensor> getSensorById(
            @Parameter(description = "Sensor ID") @PathVariable Long id) {
        return ResponseEntity.ok(sensorService.getSensorById(id));
    }
}
