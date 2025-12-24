package com.example.analyseservice.integration;

import com.example.analyseservice.dto.SensorDataDTO;
import com.example.analyseservice.dto.SensorDataResponse;
import com.example.analyseservice.entities.SensorData;
import com.example.analyseservice.repo.SensorDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Sensor Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SensorIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private SensorDataRepository sensorDataRepository;

        @MockBean
        private RestTemplate restTemplate;

        @Autowired
        private ObjectMapper objectMapper;

        private SensorDataResponse mockResponse;

        @BeforeEach
        void setUp() {
                // Create mock AI service response
                SensorDataDTO dto = new SensorDataDTO();
                dto.setSoilHumidityA4("512");
                dto.setSoilHumidityA2("487");
                dto.setGasA5("234");
                dto.setRainA3("100");
                dto.setTemperature("25.4");
                dto.setAirHumidity("65.2");
                dto.setTimestamp("2025-12-21 15:00:00");

                mockResponse = new SensorDataResponse();
                mockResponse.setStatus("success");
                mockResponse.setMessage("Sensor data retrieved successfully");
                mockResponse.setData(dto);
        }

        @AfterEach
        void tearDown() {
                // Clean up database after each test
                sensorDataRepository.deleteAll();
        }

        @Test
        @Order(1)
        @DisplayName("Integration Test - Complete Data Collection Flow")
        void testCompleteDataCollectionFlow() throws Exception {
                // Given - Mock AI service response
                when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                                .thenReturn(mockResponse);

                // When & Then - Collect data
                mockMvc.perform(post("/sensors/collect"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.soilHumidityA4").value("512"))
                                .andExpect(jsonPath("$.temperature").value("25.4"));

                // Verify data was saved to database
                long count = sensorDataRepository.count();
                Assertions.assertEquals(1, count);
        }

        @Test
        @Order(2)
        @DisplayName("Integration Test - Multiple Collections and History")
        void testMultipleCollectionsAndHistory() throws Exception {
                // Given
                when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                                .thenReturn(mockResponse);

                // Collect multiple times
                for (int i = 0; i < 5; i++) {
                        mockMvc.perform(post("/sensors/collect"))
                                        .andExpect(status().isOk());
                        Thread.sleep(100); // Small delay to ensure different timestamps
                }

                // Then - Check count
                mockMvc.perform(get("/sensors/count"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("5"));

                // Then - Check history
                mockMvc.perform(get("/sensors/history?page=0&size=10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(5)));

                // Then - Check latest
                mockMvc.perform(get("/sensors/latest"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @Order(3)
        @DisplayName("Integration Test - Pagination Functionality")
        void testPaginationFunctionality() throws Exception {
                // Given - Create 15 sensor readings
                for (int i = 0; i < 15; i++) {
                        SensorData data = new SensorData();
                        data.setSoilHumidityA4(String.valueOf(500 + i));
                        data.setSoilHumidityA2("480");
                        data.setGasA5("230");
                        data.setRainA3("100");
                        data.setTemperature("25." + i);
                        data.setAirHumidity("65.0");
                        data.setSensorTimestamp("2025-12-21 15:0" + i + ":00");
                        data.setCreatedAt(LocalDateTime.now().plusSeconds(i));
                        sensorDataRepository.save(data);
                }

                // When & Then - Test first page
                mockMvc.perform(get("/sensors/history?page=0&size=5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(5)));

                // When & Then - Test second page
                mockMvc.perform(get("/sensors/history?page=1&size=5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(5)));

                // When & Then - Test third page
                mockMvc.perform(get("/sensors/history?page=2&size=5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(5)));

                // When & Then - Test last page (overflow)
                mockMvc.perform(get("/sensors/history?page=3&size=5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @Order(4)
        @DisplayName("Integration Test - Error Handling When AI Service Fails")
        void testErrorHandlingWhenAIServiceFails() throws Exception {
                // Given - Mock AI service failure
                when(restTemplate.postForObject(anyString(), any(), eq(SensorDataResponse.class)))
                                .thenReturn(null);

                // When & Then
                mockMvc.perform(post("/sensors/collect"))
                                .andExpect(status().isServiceUnavailable());

                // Verify no data was saved
                long count = sensorDataRepository.count();
                Assertions.assertEquals(0, count);
        }

        @Test
        @Order(5)
        @DisplayName("Integration Test - Get Latest When Database Empty")
        void testGetLatestWhenDatabaseEmpty() throws Exception {
                // When & Then
                mockMvc.perform(get("/sensors/latest"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(6)
        @DisplayName("Integration Test - Get All Sensor Data")
        void testGetAllSensorData() throws Exception {
                // Given - Create sensor readings
                for (int i = 0; i < 3; i++) {
                        SensorData data = new SensorData();
                        data.setSoilHumidityA4(String.valueOf(500 + i));
                        data.setTemperature("25." + i);
                        data.setSoilHumidityA2("480");
                        data.setGasA5("230");
                        data.setRainA3("100");
                        data.setAirHumidity("65.0");
                        data.setSensorTimestamp("2025-12-21 15:00:0" + i);
                        data.setCreatedAt(LocalDateTime.now().plusSeconds(i));
                        sensorDataRepository.save(data);
                }

                // When & Then
                mockMvc.perform(get("/sensors/all"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(3)))
                                .andExpect(jsonPath("$[0].temperature").value("25.2")) // Latest first
                                .andExpect(jsonPath("$[2].temperature").value("25.0")); // Oldest last
        }

        @Test
        @Order(7)
        @DisplayName("Integration Test - Count Functionality")
        void testCountFunctionality() throws Exception {
                // Given - Initially empty
                mockMvc.perform(get("/sensors/count"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("0"));

                // When - Add data
                SensorData data = new SensorData();
                data.setSoilHumidityA4("512");
                data.setTemperature("25.4");
                data.setSoilHumidityA2("487");
                data.setGasA5("234");
                data.setRainA3("100");
                data.setAirHumidity("65.2");
                data.setSensorTimestamp("2025-12-21 15:00:00");
                data.setCreatedAt(LocalDateTime.now());
                sensorDataRepository.save(data);

                // Then
                mockMvc.perform(get("/sensors/count"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("1"));
        }
}
