package com.example.managingcaptors.controllers;

import com.example.managingcaptors.DAO.Captures;
import com.example.managingcaptors.DAO.CaptursInputs;
import com.example.managingcaptors.DAO.Info;
import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.entities.IotCaptures;
import com.example.managingcaptors.entities.Locations;
import com.example.managingcaptors.services.IotCapturesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(IotCapturesController.class)
@AutoConfigureMockMvc(addFilters = false)
class IotCapturesControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private IotCapturesService iotCapturesService;

        @MockBean
        private com.example.managingcaptors.config.JwtUtil jwtUtil;

        private User testUser;
        private IotCaptures testIotCapture;
        private CaptursInputs testCaptursInputs;

        @BeforeEach
        void setUp() {
                // Initialize test user with correct constructor
                testUser = new User(1L, "First", "Last", "test@example.com", "123", "pass", "loc", "caps", "token");

                // Initialize test IoT capture
                testIotCapture = new IotCaptures();
                testIotCapture.setId("iot123");
                testIotCapture.setName("Temperature Sensor");
                testIotCapture.setUnit("Celsius");
                testIotCapture.setStatus("Active");
                testIotCapture.setTimestamp("2024-01-01T10:00:00");
                testIotCapture.setUser(testUser);

                // Initialize CaptursInputs (must use IotCaptures entity)
                testCaptursInputs = new CaptursInputs();
                testCaptursInputs.setCaptures(testIotCapture);
        }

        @Test
        void testAddCapture_Success() throws Exception {
                // Arrange
                List<IotCaptures> capturesList = new ArrayList<>();
                capturesList.add(testIotCapture);

                // Use doReturn/when pattern for wildcards if needed, but standard when should
                // work with return types matching
                when(iotCapturesService.CreateIotCaptures(any(CaptursInputs.class), anyString()))
                                .thenReturn((List) capturesList);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Add-Capture")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCaptursInputs)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("iot123"))
                                .andExpect(jsonPath("$[0].name").value("Temperature Sensor"));
        }

        @Test
        void testAddCapture_InvalidToken() throws Exception {
                // Arrange
                when(iotCapturesService.CreateIotCaptures(any(CaptursInputs.class), anyString()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Add-Capture")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCaptursInputs)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testAddCapture_NoAuthorizationHeader() throws Exception {
                // Arrange
                when(iotCapturesService.CreateIotCaptures(any(CaptursInputs.class), isNull()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Add-Capture")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCaptursInputs)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetCapture_Success() throws Exception {
                // Arrange
                List<IotCaptures> capturesList = new ArrayList<>();
                capturesList.add(testIotCapture);

                when(iotCapturesService.getAllCaptors(anyString(), any(User.class)))
                                .thenReturn((List) capturesList);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Get-Capture")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("iot123"))
                                .andExpect(jsonPath("$[0].status").value("Active"));
        }

        @Test
        void testGetCapture_InvalidToken() throws Exception {
                // Arrange
                when(iotCapturesService.getAllCaptors(anyString(), any(User.class)))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Get-Capture")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testActivateDeactivate_Success() throws Exception {
                // Arrange
                Info info = new Info();
                info.setU(testUser);
                info.setId_cap("iot123");

                List<IotCaptures> capturesList = new ArrayList<>();
                testIotCapture.setStatus("Deactivate");
                capturesList.add(testIotCapture);

                when(iotCapturesService.ActivateCaptures(anyString(), any(User.class), anyString()))
                                .thenReturn((List) capturesList);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Activate-Deactivate")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(info)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].status").value("Deactivate"));
        }

        @Test
        void testActivateDeactivate_InvalidToken() throws Exception {
                // Arrange
                Info info = new Info();
                info.setU(testUser);
                info.setId_cap("iot123");

                when(iotCapturesService.ActivateCaptures(anyString(), any(User.class), anyString()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Activate-Deactivate")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(info)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testUpdateCaptor_Success() throws Exception {
                // Arrange
                Captures captures = new Captures();
                captures.setId("iot123");
                captures.setEmail("test@example.com");
                captures.setName("Updated Sensor");
                captures.setStatus("Deactivate");

                // In the controller, @RequestBody Captures cap is used.
                // We need to ensure serialization works for Captures DAO.

                IotCaptures updatedCapture = new IotCaptures();
                updatedCapture.setId("iot123");
                updatedCapture.setName("Updated Sensor");
                updatedCapture.setStatus("Deactivate");

                when(iotCapturesService.UpdateCaptures(anyString(), any(Captures.class)))
                                .thenReturn(updatedCapture);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Update-Captor")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(captures)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Sensor"))
                                .andExpect(jsonPath("$.status").value("Deactivate"));
        }

        @Test
        void testUpdateCaptor_InvalidToken() throws Exception {
                // Arrange
                Captures captures = new Captures();
                captures.setId("iot123");
                captures.setEmail("test@example.com");

                when(iotCapturesService.UpdateCaptures(anyString(), any(Captures.class)))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/IotCaptures/Update-Captor")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(captures)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testDeleteCaptor_Success() throws Exception {
                // Arrange
                List<IotCaptures> remainingCaptures = new ArrayList<>();

                when(iotCapturesService.deleteIot(anyString(), any(User.class), eq("iot123")))
                                .thenReturn((List) remainingCaptures);

                // Act & Assert
                mockMvc.perform(get("/IotCaptures/delete-Captor/iot123")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void testDeleteCaptor_InvalidToken() throws Exception {
                // Arrange
                when(iotCapturesService.deleteIot(anyString(), any(User.class), anyString()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(get("/IotCaptures/delete-Captor/iot123")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testDeleteCaptor_NoAuthorizationHeader() throws Exception {
                // Arrange
                when(iotCapturesService.deleteIot(isNull(), any(User.class), anyString()))
                                .thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get("/IotCaptures/delete-Captor/iot123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isOk());
        }
}
