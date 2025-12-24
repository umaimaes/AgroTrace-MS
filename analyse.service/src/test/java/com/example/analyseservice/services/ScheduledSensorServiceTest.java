package com.example.analyseservice.services;

import com.example.analyseservice.entities.SensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Scheduled Sensor Service Tests")
class ScheduledSensorServiceTest {

    @Mock
    private SensorCollectionService sensorCollectionService;

    @InjectMocks
    private ScheduledSensorService scheduledSensorService;

    private SensorData mockSensorData;

    @BeforeEach
    void setUp() {
        mockSensorData = new SensorData();
        mockSensorData.setId(1L);
        mockSensorData.setTemperature("25.4");
    }

    @Test
    @DisplayName("Collect Sensor Data Scheduled - Success")
    void testCollectSensorDataScheduled_Success() {
        // Given
        when(sensorCollectionService.collectAndSaveSensorData()).thenReturn(mockSensorData);

        // When
        scheduledSensorService.collectSensorDataScheduled();

        // Then
        verify(sensorCollectionService, times(1)).collectAndSaveSensorData();
    }

    @Test
    @DisplayName("Collect Sensor Data Scheduled - Null Result")
    void testCollectSensorDataScheduled_NullResult() {
        // Given
        when(sensorCollectionService.collectAndSaveSensorData()).thenReturn(null);

        // When
        scheduledSensorService.collectSensorDataScheduled();

        // Then
        verify(sensorCollectionService, times(1)).collectAndSaveSensorData();
        // Should not throw exception, just log warning
    }

    @Test
    @DisplayName("Collect Sensor Data Scheduled - Exception Handling")
    void testCollectSensorDataScheduled_ExceptionHandling() {
        // Given
        when(sensorCollectionService.collectAndSaveSensorData())
                .thenThrow(new RuntimeException("Service unavailable"));

        // When
        scheduledSensorService.collectSensorDataScheduled();

        // Then
        verify(sensorCollectionService, times(1)).collectAndSaveSensorData();
        // Should catch exception and continue
    }

    @Test
    @DisplayName("Collect Sensor Data Scheduled - Multiple Calls")
    void testCollectSensorDataScheduled_MultipleCalls() {
        // Given
        when(sensorCollectionService.collectAndSaveSensorData())
                .thenReturn(mockSensorData)
                .thenReturn(null)
                .thenReturn(mockSensorData);

        // When
        scheduledSensorService.collectSensorDataScheduled();
        scheduledSensorService.collectSensorDataScheduled();
        scheduledSensorService.collectSensorDataScheduled();

        // Then
        verify(sensorCollectionService, times(3)).collectAndSaveSensorData();
    }
}
