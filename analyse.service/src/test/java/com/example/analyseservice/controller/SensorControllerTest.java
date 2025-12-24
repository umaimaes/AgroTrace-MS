package com.example.analyseservice.controller;

import com.example.analyseservice.entities.SensorData;
import com.example.analyseservice.repo.SensorDataRepository;
import com.example.analyseservice.services.SensorCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(SensorController.class)
@DisplayName("Sensor Controller Tests")
class SensorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SensorCollectionService sensorCollectionService;

    @MockBean
    private SensorDataRepository sensorDataRepository;

    private SensorData mockSensorData;
    private List<SensorData> mockSensorDataList;

    @BeforeEach
    void setUp() {
        // Create mock sensor data
        mockSensorData = new SensorData();
        mockSensorData.setId(1L);
        mockSensorData.setSoilHumidityA4("512");
        mockSensorData.setSoilHumidityA2("487");
        mockSensorData.setGasA5("234");
        mockSensorData.setRainA3("100");
        mockSensorData.setTemperature("25.4");
        mockSensorData.setAirHumidity("65.2");
        mockSensorData.setSensorTimestamp("2025-12-21 15:00:00");
        mockSensorData.setCreatedAt(LocalDateTime.now());

        // Create mock list
        SensorData mockSensorData2 = new SensorData();
        mockSensorData2.setId(2L);
        mockSensorData2.setSoilHumidityA4("515");
        mockSensorData2.setSoilHumidityA2("490");
        mockSensorData2.setGasA5("240");
        mockSensorData2.setRainA3("105");
        mockSensorData2.setTemperature("25.8");
        mockSensorData2.setAirHumidity("66.0");
        mockSensorData2.setSensorTimestamp("2025-12-21 15:01:00");
        mockSensorData2.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        mockSensorDataList = Arrays.asList(mockSensorData, mockSensorData2);
    }

    @Test
    @DisplayName("POST /sensors/collect - Success")
    void testCollectSensorData_Success() throws Exception {
        // Given
        when(sensorCollectionService.collectAndSaveSensorData()).thenReturn(mockSensorData);

        // When & Then
        mockMvc.perform(post("/sensors/collect")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.soilHumidityA4").value("512"))
                .andExpect(jsonPath("$.soilHumidityA2").value("487"))
                .andExpect(jsonPath("$.gasA5").value("234"))
                .andExpect(jsonPath("$.rainA3").value("100"))
                .andExpect(jsonPath("$.temperature").value("25.4"))
                .andExpect(jsonPath("$.airHumidity").value("65.2"))
                .andExpect(jsonPath("$.sensorTimestamp").value("2025-12-21 15:00:00"));
    }

    @Test
    @DisplayName("POST /sensors/collect - Failure (AI Service Unavailable)")
    void testCollectSensorData_Failure() throws Exception {
        // Given
        when(sensorCollectionService.collectAndSaveSensorData()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/sensors/collect")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("Failed to collect sensor data")));
    }

    @Test
    @DisplayName("GET /sensors/latest - Success")
    void testGetLatestReading_Success() throws Exception {
        // Given
        when(sensorCollectionService.getLatestReading()).thenReturn(mockSensorData);

        // When & Then
        mockMvc.perform(get("/sensors/latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.temperature").value("25.4"));
    }

    @Test
    @DisplayName("GET /sensors/latest - Not Found")
    void testGetLatestReading_NotFound() throws Exception {
        // Given
        when(sensorCollectionService.getLatestReading()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/sensors/latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /sensors/history - Default Pagination")
    void testGetSensorHistory_DefaultPagination() throws Exception {
        // Given
        Page<SensorData> page = new PageImpl<>(mockSensorDataList);
        when(sensorDataRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/sensors/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /sensors/history - Custom Pagination")
    void testGetSensorHistory_CustomPagination() throws Exception {
        // Given
        Page<SensorData> page = new PageImpl<>(List.of(mockSensorData));
        when(sensorDataRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/sensors/history")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /sensors/all - Success")
    void testGetAllSensorData_Success() throws Exception {
        // Given
        when(sensorDataRepository.findAllByOrderByCreatedAtDesc()).thenReturn(mockSensorDataList);

        // When & Then
        mockMvc.perform(get("/sensors/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].temperature").value("25.4"))
                .andExpect(jsonPath("$[1].temperature").value("25.8"));
    }

    @Test
    @DisplayName("GET /sensors/all - Empty List")
    void testGetAllSensorData_EmptyList() throws Exception {
        // Given
        when(sensorDataRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/sensors/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /sensors/count - With Data")
    void testGetCount_WithData() throws Exception {
        // Given
        when(sensorDataRepository.count()).thenReturn(42L);

        // When & Then
        mockMvc.perform(get("/sensors/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @DisplayName("GET /sensors/count - Zero Data")
    void testGetCount_ZeroData() throws Exception {
        // Given
        when(sensorDataRepository.count()).thenReturn(0L);

        // When & Then
        mockMvc.perform(get("/sensors/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}
