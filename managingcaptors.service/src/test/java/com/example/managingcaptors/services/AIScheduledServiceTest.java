package com.example.managingcaptors.services;

import com.example.managingcaptors.entities.MesuresEachPlant;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.repo.MesuresEachPlantRepo;
import com.example.managingcaptors.repo.PlantRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIScheduledServiceTest {

    @Mock
    private PlantRepo plantRepo;

    @Mock
    private MesuresEachPlantRepo mesuresEachPlantRepo;

    @Mock
    private MesurePlantService mesurePlantService;

    @InjectMocks
    private AIScheduledService aiScheduledService;

    private Plant plant1;
    private Plant plant2;
    private MesuresEachPlant measurement1;

    @BeforeEach
    void setUp() {
        plant1 = new Plant();
        plant1.setId("1");
        plant1.setName("Tomato");

        plant2 = new Plant();
        plant2.setId("2");
        plant2.setName("Potato");

        measurement1 = new MesuresEachPlant();
        measurement1.setId("m1");
        measurement1.setPlant(plant1);
    }

    @Test
    void testSendMeasurementsToAI_WithMeasurements() {
        // Arrange
        when(plantRepo.findAll()).thenReturn(Arrays.asList(plant1, plant2));
        when(mesuresEachPlantRepo.findTopByPlantOrderByDateTimeDesc(plant1)).thenReturn(measurement1);
        when(mesuresEachPlantRepo.findTopByPlantOrderByDateTimeDesc(plant2)).thenReturn(null);

        // Act
        aiScheduledService.sendMeasurementsToAI();

        // Assert
        verify(mesurePlantService, times(1)).getRecommendation(measurement1);
        verify(mesurePlantService, never()).getRecommendation(null);
    }

    @Test
    void testSendMeasurementsToAI_NoPlants() {
        // Arrange
        when(plantRepo.findAll()).thenReturn(Collections.emptyList());

        // Act
        aiScheduledService.sendMeasurementsToAI();

        // Assert
        verify(mesuresEachPlantRepo, never()).findTopByPlantOrderByDateTimeDesc(any());
        verify(mesurePlantService, never()).getRecommendation(any());
    }

    @Test
    void testSendMeasurementsToAI_ExceptionHandling() {
        // Arrange
        when(plantRepo.findAll()).thenReturn(Arrays.asList(plant1, plant2));
        when(mesuresEachPlantRepo.findTopByPlantOrderByDateTimeDesc(plant1))
                .thenThrow(new RuntimeException("Database error"));
        when(mesuresEachPlantRepo.findTopByPlantOrderByDateTimeDesc(plant2)).thenReturn(null);

        // Act
        aiScheduledService.sendMeasurementsToAI();

        // Assert
        // Should continue to next plant despite exception
        verify(mesuresEachPlantRepo).findTopByPlantOrderByDateTimeDesc(plant2);
    }
}
