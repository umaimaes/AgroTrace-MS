package com.example.analyseservice.services;

import com.example.analyseservice.dto.SensorDataDTO;
import com.example.analyseservice.dto.SensorDataResponse;
import com.example.analyseservice.entities.SensorData;
import com.example.analyseservice.repo.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SensorCollectionService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Value("${ai.service.sensor.endpoint}")
    private String sensorEndpoint;

    /**
     * Fetch sensor data from AI service and save to database
     * 
     * @return SensorData entity if successful, null otherwise
     */
    public SensorData collectAndSaveSensorData() {
        try {
            // 1. Call AI Service
            String fullUrl = aiServiceUrl + sensorEndpoint;
            System.out.println("📡 Calling AI Service: " + fullUrl);

            SensorDataResponse response = restTemplate.postForObject(
                    fullUrl,
                    null,
                    SensorDataResponse.class);

            // 2. Validate Response
            if (response == null || response.getData() == null) {
                System.err.println("✗ No data received from AI service");
                return null;
            }

            if (!"success".equalsIgnoreCase(response.getStatus())) {
                System.err.println("✗ AI service returned error status: " + response.getStatus());
                return null;
            }

            SensorDataDTO dto = response.getData();

            // 3. Map DTO to Entity
            SensorData sensorData = new SensorData();
            sensorData.setSoilHumidityA4(dto.getSoilHumidityA4());
            sensorData.setSoilHumidityA2(dto.getSoilHumidityA2());
            sensorData.setGasA5(dto.getGasA5());
            sensorData.setRainA3(dto.getRainA3());
            sensorData.setTemperature(dto.getTemperature());
            sensorData.setAirHumidity(dto.getAirHumidity());
            sensorData.setSensorTimestamp(dto.getTimestamp());

            // 4. Save to Database
            SensorData saved = sensorDataRepository.save(sensorData);
            System.out.println("✓ Sensor data saved to database (ID: " + saved.getId() + ")");

            // 5. Display Data
            displaySensorData(dto);

            return saved;

        } catch (RestClientException e) {
            System.err.println("✗ Error calling AI service: " + e.getMessage());
            System.err.println("  Make sure AI service is running on " + aiServiceUrl);
            return null;
        } catch (Exception e) {
            System.err.println("✗ Unexpected error collecting sensor data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Display sensor data in formatted output
     */
    private void displaySensorData(SensorDataDTO data) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║          SENSOR DATA COLLECTED                   ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("  Timestamp       : " + data.getTimestamp());
        System.out.println("  Soil Humidity A4: " + data.getSoilHumidityA4());
        System.out.println("  Soil Humidity A2: " + data.getSoilHumidityA2());
        System.out.println("  Gas Level (A5)  : " + data.getGasA5());
        System.out.println("  Rain Level (A3) : " + data.getRainA3());
        System.out.println("  Temperature     : " + data.getTemperature() + " °C");
        System.out.println("  Air Humidity    : " + data.getAirHumidity() + " %");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    /**
     * Get latest sensor reading from database
     */
    public SensorData getLatestReading() {
        return sensorDataRepository.findTopByOrderByCreatedAtDesc();
    }
}
