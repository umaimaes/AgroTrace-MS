package com.example.analyseservice.controller;

import com.example.analyseservice.entities.SensorData;
import com.example.analyseservice.repo.SensorDataRepository;
import com.example.analyseservice.services.SensorCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    @Autowired
    private SensorCollectionService sensorCollectionService;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    /**
     * Manually trigger sensor data collection
     * POST /sensors/collect
     */
    @PostMapping("/collect")
    public ResponseEntity<?> collectSensorData() {
        SensorData data = sensorCollectionService.collectAndSaveSensorData();

        if (data != null) {
            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.status(503)
                    .body("Failed to collect sensor data. Check if AI service is running.");
        }
    }

    /**
     * Get latest sensor reading
     * GET /sensors/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestReading() {
        SensorData latest = sensorCollectionService.getLatestReading();

        if (latest != null) {
            return ResponseEntity.ok(latest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get sensor history with pagination
     * GET /sensors/history?page=0&size=10
     */
    @GetMapping("/history")
    public ResponseEntity<List<SensorData>> getSensorHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<SensorData> history = sensorDataRepository.findAll(pageRequest).getContent();
        return ResponseEntity.ok(history);
    }

    /**
     * Get all sensor data
     * GET /sensors/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<SensorData>> getAllSensorData() {
        List<SensorData> allData = sensorDataRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(allData);
    }

    /**
     * Get total count of sensor readings
     * GET /sensors/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getCount() {
        long count = sensorDataRepository.count();
        return ResponseEntity.ok(count);
    }
}
