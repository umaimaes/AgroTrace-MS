package com.example.analyseservice.services;

import com.example.analyseservice.dto.SensorDataDTO;
import com.example.analyseservice.dto.SensorDataResponse;
import com.example.analyseservice.entities.SensorData;
import com.example.analyseservice.repo.SensorDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sensor Collection Service Tests")
class SensorCollectionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SensorDataRepository sensorDataRepository;

    @InjectMocks
    private SensorCollectionService sensorCollectionService;

    private SensorDataResponse mockResponse;
    private SensorDataDTO mockDTO;
    private SensorData mockSavedData;

    @BeforeEach
    void setUp() {
        // Set configuration properties
        ReflectionTestUtils.setField(sensorCollectionService, "aiServiceUrl", "http://localhost:8000");
        ReflectionTestUtils.setField(sensorCollectionService, "sensorEndpoint", "/sensors/read");

        // Create mock DTO
        mockDTO = new SensorDataDTO();
        mockDTO.setSoilHumidityA4("512");
        mockDTO.setSoilHumidityA2("487");
        mockDTO.setGasA5("234");
        mockDTO.setRainA3("100");
        mockDTO.setTemperature("25.4");
        mockDTO.setAirHumidity("65.2");
        mockDTO.setTimestamp("2025-12-21 15:00:00");

        // Create mock response
        mockResponse = new SensorDataResponse();
        mockResponse.setStatus("success");
        mockResponse.setMessage("Sensor data retrieved successfully");
        mockResponse.setData(mockDTO);

        // Create mock saved data
        mockSavedData = new SensorData();
        mockSavedData.setId(1L);
        mockSavedData.setSoilHumidityA4("512");
        mockSavedData.setSoilHumidityA2("487");
        mockSavedData.setGasA5("234");
        mockSavedData.setRainA3("100");
        mockSavedData.setTemperature("25.4");
        mockSavedData.setAirHumidity("65.2");
        mockSavedData.setSensorTimestamp("2025-12-21 15:00:00");
    }

    @Test
    @DisplayName("Collect and Save Sensor Data - Success")
    void testCollectAndSaveSensorData_Success() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                .thenReturn(mockResponse);
        when(sensorDataRepository.save(any(SensorData.class))).thenReturn(mockSavedData);

        // When
        SensorData result = sensorCollectionService.collectAndSaveSensorData();

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("512", result.getSoilHumidityA4());
        assertEquals("25.4", result.getTemperature());
        
        // Verify interactions
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8000/sensors/read"),
                isNull(),
                eq(SensorDataResponse.class)
        );
        verify(sensorDataRepository, times(1)).save(any(SensorData.class));
    }

    @Test
    @DisplayName("Collect and Save Sensor Data - Null Response")
    void testCollectAndSaveSensorData_NullResponse() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                .thenReturn(null);

        // When
        SensorData result = sensorCollectionService.collectAndSaveSensorData();

        // Then
        assertNull(result);
        verify(sensorDataRepository, never()).save(any(SensorData.class));
    }

    @Test
    @DisplayName("Collect and Save Sensor Data - Null Data in Response")
    void testCollectAndSaveSensorData_NullDataInResponse() {
        // Given
        mockResponse.setData(null);
        when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                .thenReturn(mockResponse);

        // When
        SensorData result = sensorCollectionService.collectAndSaveSensorData();

        // Then
        assertNull(result);
        verify(sensorDataRepository, never()).save(any(SensorData.class));
    }

    @Test
    @DisplayName("Collect and Save Sensor Data - Error Status")
    void testCollectAndSaveSensorData_ErrorStatus() {
        // Given
        mockResponse.setStatus("error");
        when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                .thenReturn(mockResponse);

        // When
        SensorData result = sensorCollectionService.collectAndSaveSensorData();

        // Then
        assertNull(result);
        verify(sensorDataRepository, never()).save(any(SensorData.class));
    }

    @Test
    @DisplayName("Collect and Save Sensor Data - RestClientException")
    void testCollectAndSaveSensorData_RestClientException() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // When
        SensorData result = sensorCollectionService.collectAndSaveSensorData();

        // Then
        assertNull(result);
        verify(sensorDataRepository, never()).save(any(SensorData.class));
    }

    @Test
    @DisplayName("Collect and Save Sensor Data - Generic Exception")
    void testCollectAndSaveSensorData_GenericException() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When
        SensorData result = sensorCollectionService.collectAndSaveSensorData();

        // Then
        assertNull(result);
        verify(sensorDataRepository, never()).save(any(SensorData.class));
    }

    @Test
    @DisplayName("Collect and Save Sensor Data - Verify Data Mapping")
    void testCollectAndSaveSensorData_VerifyDataMapping() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                .thenReturn(mockResponse);
        when(sensorDataRepository.save(any(SensorData.class))).thenReturn(mockSavedData);

        // When
        sensorCollectionService.collectAndSaveSensorData();

        // Then - Capture and verify the saved entity
        ArgumentCaptor<SensorData> captor = ArgumentCaptor.forClass(SensorData.class);
        verify(sensorDataRepository).save(captor.capture());
        
        SensorData captured = captor.getValue();
        assertEquals("512", captured.getSoilHumidityA4());
        assertEquals("487", captured.getSoilHumidityA2());
        assertEquals("234", captured.getGasA5());
        assertEquals("100", captured.getRainA3());
        assertEquals("25.4", captured.getTemperature());
        assertEquals("65.2", captured.getAirHumidity());
        assertEquals("2025-12-21 15:00:00", captured.getSensorTimestamp());
    }

    @Test
    @DisplayName("Get Latest Reading - Success")
    void testGetLatestReading_Success() {
        // Given
        when(sensorDataRepository.findTopByOrderByCreatedAtDesc()).thenReturn(mockSavedData);

        // When
        SensorData result = sensorCollectionService.getLatestReading();

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(sensorDataRepository, times(1)).findTopByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Get Latest Reading - No Data")
    void testGetLatestReading_NoData() {
        // Given
        when(sensorDataRepository.findTopByOrderByCreatedAtDesc()).thenReturn(null);

        // When
        SensorData result = sensorCollectionService.getLatestReading();

        // Then
        assertNull(result);
        verify(sensorDataRepository, times(1)).findTopByOrderByCreatedAtDesc();
    }
}
