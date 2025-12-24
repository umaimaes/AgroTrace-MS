package com.example.managingcaptors.controllers;

import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.DAO.newPlant;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.services.PlantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(PlantController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlantControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PlantService plantService;

        @MockBean
        private com.example.managingcaptors.config.JwtUtil jwtUtil;

        private User testUser;
        private Plant testPlant;
        private newPlant testNewPlant;

        @BeforeEach
        void setUp() {
                // Initialize test user with correct constructor
                testUser = new User(1L, "First", "Last", "test@example.com", "123", "pass", "loc", "caps", "token");

                // Initialize test plant
                testPlant = new Plant();
                testPlant.setId("plant123");
                testPlant.setName("Tomato");
                testPlant.setUser(testUser);

                // Initialize new plant DAO
                testNewPlant = new newPlant();
                testNewPlant.setName("Tomato");
                testNewPlant.setUser(testUser);
                testNewPlant.setLatitude(34.0522);
                testNewPlant.setLongitude(-118.2437);
        }

        @Test
        void testGetPlant_Success() throws Exception {
                // Arrange
                List<Plant> plantList = new ArrayList<>();
                plantList.add(testPlant);

                when(plantService.getPlants(anyString(), any(User.class)))
                                .thenReturn(plantList);

                // Act & Assert
                mockMvc.perform(post("/Plant/get-plant")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("plant123"))
                                .andExpect(jsonPath("$[0].name").value("Tomato"));
        }

        @Test
        void testGetPlant_InvalidToken() throws Exception {
                // Arrange
                when(plantService.getPlants(anyString(), any(User.class)))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/Plant/get-plant")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testGetPlant_EmptyList() throws Exception {
                // Arrange
                List<Plant> emptyList = new ArrayList<>();

                when(plantService.getPlants(anyString(), any(User.class)))
                                .thenReturn(emptyList);

                // Act & Assert
                mockMvc.perform(post("/Plant/get-plant")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void testGetPlant_NoAuthorizationHeader() throws Exception {
                // Arrange
                when(plantService.getPlants(isNull(), any(User.class)))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/Plant/get-plant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreatePlant_Success() throws Exception {
                // Arrange
                List<Plant> plantList = new ArrayList<>();
                plantList.add(testPlant);

                when(plantService.insertPlant(any(newPlant.class), anyString()))
                                .thenReturn(plantList);

                // Act & Assert
                mockMvc.perform(post("/Plant/create-Plant")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testNewPlant)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("plant123"))
                                .andExpect(jsonPath("$[0].name").value("Tomato"));
        }

        @Test
        void testCreatePlant_InvalidToken() throws Exception {
                // Arrange
                when(plantService.insertPlant(any(newPlant.class), anyString()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/Plant/create-Plant")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testNewPlant)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testUpdatePlant_Success() throws Exception {
                // Arrange
                Plant updatedPlant = new Plant();
                updatedPlant.setId("plant123");
                updatedPlant.setName("Updated Tomato");
                updatedPlant.setUser(testUser);

                List<Plant> plantList = new ArrayList<>();
                plantList.add(updatedPlant);

                when(plantService.updatePlant(any(Plant.class), anyString()))
                                .thenReturn(plantList);

                // Act & Assert
                mockMvc.perform(post("/Plant/update-Plant")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedPlant)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Updated Tomato"));
        }

        @Test
        void testUpdatePlant_InvalidToken() throws Exception {
                // Arrange
                Plant updatedPlant = new Plant();
                updatedPlant.setId("plant123");
                updatedPlant.setUser(testUser);

                when(plantService.updatePlant(any(Plant.class), anyString()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/Plant/update-Plant")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedPlant)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testUpdatePlant_PlantNotFound() throws Exception {
                // Arrange
                Plant updatedPlant = new Plant();
                updatedPlant.setId("non-existent");
                updatedPlant.setUser(testUser);

                when(plantService.updatePlant(any(Plant.class), anyString()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/Plant/update-Plant")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedPlant)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testDeletePlant_Success() throws Exception {
                // Arrange
                List<Plant> remainingPlants = new ArrayList<>();

                when(plantService.deletePlant(eq("plant123"), anyString(), any(User.class)))
                                .thenReturn(remainingPlants);

                // Act & Assert
                mockMvc.perform(get("/Plant/delete-Plant/plant123")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void testDeletePlant_InvalidToken() throws Exception {
                // Arrange
                when(plantService.deletePlant(anyString(), anyString(), any(User.class)))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(get("/Plant/delete-Plant/plant123")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testDeletePlant_PlantNotFound() throws Exception {
                // Arrange
                when(plantService.deletePlant(eq("non-existent"), anyString(), any(User.class)))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(get("/Plant/delete-Plant/non-existent")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testDeletePlant_NoAuthorizationHeader() throws Exception {
                // Arrange
                when(plantService.deletePlant(anyString(), isNull(), any(User.class)))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(get("/Plant/delete-Plant/plant123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testUser)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreatePlant_WithCompleteData() throws Exception {
                // Arrange
                newPlant completePlant = new newPlant();
                completePlant.setName("Cucumber");
                completePlant.setUser(testUser);
                completePlant.setLatitude(40.7128);
                completePlant.setLongitude(-74.0060);

                Plant createdPlant = new Plant();
                createdPlant.setId("plant456");
                createdPlant.setName("Cucumber");
                createdPlant.setUser(testUser);

                List<Plant> plantList = new ArrayList<>();
                plantList.add(createdPlant);

                when(plantService.insertPlant(any(newPlant.class), anyString()))
                                .thenReturn(plantList);

                // Act & Assert
                mockMvc.perform(post("/Plant/create-Plant")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(completePlant)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Cucumber"));
        }
}
