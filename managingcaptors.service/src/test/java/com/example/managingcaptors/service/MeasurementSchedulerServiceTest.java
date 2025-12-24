package com.example.managingcaptors.service;

import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.entities.MesuresEachPlant;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.repo.MesuresEachPlantRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeasurementSchedulerServiceTest {

    @Mock
    private MesuresEachPlantRepo mesuresEachPlantRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MeasurementSchedulerService measurementSchedulerService;

    private Plant testPlant;
    private User testUser;
    private MesuresEachPlant testMeasurement;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "First", "Last", "test@example.com", "123", "pass", "loc", "caps", "token");

        testPlant = new Plant();
        testPlant.setId("plant1");
        testPlant.setName("Test Plant");

        testMeasurement = new MesuresEachPlant();
        testMeasurement.setId("m1");
        testMeasurement.setPlant(testPlant);
        testMeasurement.setUser(testUser);
        testMeasurement.setTemperature(25.0);
        testMeasurement.setHumidity(60.0);
    }

    @Test
    void testSendMeasurementsToAI_Success() {
        // Arrange
        List<MesuresEachPlant> measurements = new ArrayList<>();
        measurements.add(testMeasurement);

        when(mesuresEachPlantRepo.findLatestMeasurements()).thenReturn(measurements);
        when(restTemplate.postForObject(any(String.class), any(Map.class), eq(Map.class)))
                .thenReturn(Collections.singletonMap("status", "success"));

        // Act
        measurementSchedulerService.sendMeasurementsToAI();

        // Assert
        verify(mesuresEachPlantRepo, times(1)).findLatestMeasurements();
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8000/recommend"),
                any(Map.class),
                eq(Map.class));
    }

    @Test
    void testSendMeasurementsToAI_NoMeasurements() {
        // Arrange
        when(mesuresEachPlantRepo.findLatestMeasurements()).thenReturn(Collections.emptyList());

        // Act
        measurementSchedulerService.sendMeasurementsToAI();

        // Assert
        verify(mesuresEachPlantRepo, times(1)).findLatestMeasurements();
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void testSendMeasurementsToAI_RepoException() {
        // Arrange
        when(mesuresEachPlantRepo.findLatestMeasurements()).thenThrow(new RuntimeException("DB Error"));

        // Act
        measurementSchedulerService.sendMeasurementsToAI();

        // Assert
        verify(mesuresEachPlantRepo, times(1)).findLatestMeasurements();
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void testSendMeasurementsToAI_RestTemplateException() {
        // Arrange
        List<MesuresEachPlant> measurements = new ArrayList<>();
        measurements.add(testMeasurement);
        MesuresEachPlant measurement2 = new MesuresEachPlant();
        measurement2.setId("m2");
        Plant plant2 = new Plant();
        plant2.setId("plant2");
        measurement2.setPlant(plant2);
        measurement2.setUser(testUser);
        measurements.add(measurement2);

        when(mesuresEachPlantRepo.findLatestMeasurements()).thenReturn(measurements);

        // First call throws exception, second succeeds
        when(restTemplate.postForObject(any(String.class), any(Map.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Network Error"))
                .thenReturn(Collections.singletonMap("status", "success"));

        // Act
        measurementSchedulerService.sendMeasurementsToAI();

        // Assert
        verify(mesuresEachPlantRepo, times(1)).findLatestMeasurements();
        // Should attempt to send both despite first one failing
        verify(restTemplate, times(2)).postForObject(
                eq("http://localhost:8000/recommend"),
                any(Map.class),
                eq(Map.class));
    }
}
