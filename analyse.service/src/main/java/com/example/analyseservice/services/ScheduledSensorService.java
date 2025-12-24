package com.example.analyseservice.services;

import com.example.analyseservice.entities.SensorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ScheduledSensorService {

    @Autowired
    private SensorCollectionService sensorCollectionService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Scheduled task to collect sensor data every 1 minute
     * fixedRate = 60000 milliseconds = 60 seconds = 1 minute
     */
    @Scheduled(fixedRate = 60000)
    public void collectSensorDataScheduled() {
        String timestamp = LocalDateTime.now().format(formatter);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("⏰ SCHEDULED TASK - Collecting Sensor Data");
        System.out.println("   Time: " + timestamp);
        System.out.println("=".repeat(60));

        try {
            SensorData data = sensorCollectionService.collectAndSaveSensorData();

            if (data != null) {
                System.out.println("✓ Scheduled sensor collection completed successfully");
            } else {
                System.err.println("⚠ Scheduled sensor collection completed with warnings (check logs above)");
            }
        } catch (Exception e) {
            System.err.println("✗ Error in scheduled sensor collection: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=".repeat(60) + "\n");
    }
}
