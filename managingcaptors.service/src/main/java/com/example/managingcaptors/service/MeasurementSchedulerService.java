package com.example.managingcaptors.service;

import com.example.managingcaptors.entities.MesuresEachPlant;
import com.example.managingcaptors.repo.MesuresEachPlantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class MeasurementSchedulerService {

    private static final Logger logger = Logger.getLogger(MeasurementSchedulerService.class.getName());
    private static final String AI_SERVICE_URL = "http://localhost:8000/recommend";

    @Autowired
    private MesuresEachPlantRepo mesuresEachPlantRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 8 * 60 * 60 * 1000) // Every 8 hours
    public void sendMeasurementsToAI() {
        logger.info("Starting scheduled measurement transmission to AI service...");
        
        try {
            // Get the latest measurements for each plant
            List<MesuresEachPlant> latestMeasurements = getLatestMeasurementsForAllPlants();
            
            if (latestMeasurements.isEmpty()) {
                logger.info("No measurements found to send to AI service");
                return;
            }

            for (MesuresEachPlant measurement : latestMeasurements) {
                sendSingleMeasurement(measurement);
            }
            
            logger.info("Completed sending " + latestMeasurements.size() + " measurements to AI service");
            
        } catch (Exception e) {
            logger.severe("Error in scheduled measurement transmission: " + e.getMessage());
        }
    }

    private List<MesuresEachPlant> getLatestMeasurementsForAllPlants() {
        // Get latest measurement per plant - you might need to implement this logic
        // For now, get all measurements from the last 24 hours
        return mesuresEachPlantRepo.findLatestMeasurements();
    }

    private void sendSingleMeasurement(MesuresEachPlant measurement) {
        try {
            // Prepare measurement data for AI service
            Map<String, Object> measurementData = prepareMeasurementData(measurement);
            
            logger.info("Sending measurement for plant ID: " + measurement.getPlant().getId());
            
            // Send to AI service
            Map<String, Object> response = restTemplate.postForObject(
                AI_SERVICE_URL,
                measurementData,
                Map.class
            );
            
            logger.info("Successfully sent measurement for plant ID: " + measurement.getPlant().getId() + 
                       ". Response: " + response);
            
        } catch (Exception e) {
            logger.severe("Failed to send measurement for plant ID " + measurement.getPlant().getId() + 
                         ": " + e.getMessage());
        }
    }

    private Map<String, Object> prepareMeasurementData(MesuresEachPlant measurement) {
        Map<String, Object> data = new HashMap<>();
        
        // Metadata for notification
        data.put("plant_id", measurement.getPlant().getId());
        data.put("plant_name", measurement.getPlant().getName() != null ? measurement.getPlant().getName() : "Plant_" + measurement.getPlant().getId());
        data.put("user_email", measurement.getUser().getEmail() != null ? measurement.getUser().getEmail() : "user@example.com");
        
        // Climatic Data - mapping from measurement entity
        data.put("stage", measurement.getCropCoefficientStage() != null ? measurement.getCropCoefficientStage().intValue() : 1);
        data.put("temperature", measurement.getTemperature() != null ? measurement.getTemperature() : 25.0);
        data.put("humidity", measurement.getHumidity() != null ? measurement.getHumidity() : 60.0);
        data.put("soil_moisture", measurement.getSoilMoisture() != null ? measurement.getSoilMoisture() : 50.0);
        data.put("nitrogen", measurement.getNitrogen() != null ? measurement.getNitrogen() : 0.0);
        data.put("phosphorus", measurement.getPhosphorus() != null ? measurement.getPhosphorus() : 0.0);
        data.put("potassium", measurement.getPotassium() != null ? measurement.getPotassium() : 0.0);
        data.put("ph", measurement.getPH() != null ? measurement.getPH() : 6.5);
        data.put("solar_radiation", measurement.getSolarRadiationGhi() != null ? measurement.getSolarRadiationGhi() : 0.0);
        data.put("wind_speed", measurement.getWindSpeed() != null ? measurement.getWindSpeed() : 0.0);
        
        return data;
    }
}
