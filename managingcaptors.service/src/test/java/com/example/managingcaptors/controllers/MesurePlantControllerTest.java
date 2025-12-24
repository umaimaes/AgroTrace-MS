package com.example.managingcaptors.controllers;

import com.example.managingcaptors.DAO.PlantInput;
import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.DAO.plantInfo;
import com.example.managingcaptors.entities.MesuresEachPlant;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.services.MesurePlantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(MesurePlantController.class)
@AutoConfigureMockMvc(addFilters = false)
class MesurePlantControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private MesurePlantService mesurePlantService;

        @MockBean
        private com.example.managingcaptors.config.JwtUtil jwtUtil;

        private User testUser;
        private Plant testPlant;
        private PlantInput testPlantInput;
        private MesuresEachPlant testMesure;

        @BeforeEach
        void setUp() {
                // Initialize test user with correct constructor
                testUser = new User(1L, "First", "Last", "test@example.com", "123", "pass", "loc", "caps", "token");

                // Initialize test plant
                testPlant = new Plant();
                testPlant.setId("plant123");
                testPlant.setName("Tomato");
                testPlant.setUser(testUser);

                // Initialize test plant input
                testPlantInput = new PlantInput();
                testPlantInput.setUser(testUser);
                testPlantInput.setPlant(testPlant);
                testPlantInput.setTemperature(25.5);
                testPlantInput.setHumidity(65.0);
                testPlantInput.setSoilMoisture(45.0);
                testPlantInput.setReferenceEvapotranspiration(3.5);
                testPlantInput.setEvapotranspiration(2.8);
                testPlantInput.setCropCoefficient(0.9);
                testPlantInput.setCropCoefficientStage(0.5); // Fixed: set double
                testPlantInput.setNitrogen(20.0);
                testPlantInput.setPhosphorus(15.0);
                testPlantInput.setPotassium(30.0);
                testPlantInput.setSolarRadiationGhi(500.0);
                testPlantInput.setWindSpeed(3.2);
                testPlantInput.setDaysOfPlanted(45);
                testPlantInput.setPH(6.5);

                // Initialize test mesure
                testMesure = new MesuresEachPlant();
                testMesure.setId("mesure123");
                testMesure.setUser(testUser);
                testMesure.setPlant(testPlant);
                testMesure.setTemperature(25.5);
                testMesure.setHumidity(65.0);
                testMesure.setSoilMoisture(45.0);
                testMesure.setCropCoefficientStage(0.5); // Fixed: set double
                testMesure.setNitrogen(20.0);
                testMesure.setPhosphorus(15.0);
                testMesure.setPotassium(30.0);
                testMesure.setPH(6.5);
                testMesure.setSolarRadiationGhi(500.0);
                testMesure.setWindSpeed(3.2);
        }

        @Test
        void testInsertMesurePlant_Success() throws Exception {
                // Arrange
                List<MesuresEachPlant> mesuresList = new ArrayList<>();
                mesuresList.add(testMesure);

                when(mesurePlantService.InsertIotCapturesMesurePlant(any(PlantInput.class), anyString()))
                                .thenReturn((List) mesuresList);

                // Act & Assert
                mockMvc.perform(post("/MesurePlant/insert")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testPlantInput)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("mesure123"))
                                .andExpect(jsonPath("$[0].Temperature").value(25.5))
                                .andExpect(jsonPath("$[0].Humidity").value(65.0));
        }

        @Test
        void testInsertMesurePlant_InvalidToken() throws Exception {
                // Arrange
                when(mesurePlantService.InsertIotCapturesMesurePlant(any(PlantInput.class), anyString()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/MesurePlant/insert")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testPlantInput)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testGetMesurePlant_Success() throws Exception {
                // Arrange
                plantInfo plantInfo = new plantInfo();
                plantInfo.setUser(testUser);
                plantInfo.setPlant(testPlant);

                List<MesuresEachPlant> mesuresList = new ArrayList<>();
                mesuresList.add(testMesure);

                when(mesurePlantService.getIotCaptureMesurePlant(any(plantInfo.class), anyString()))
                                .thenReturn((List) mesuresList);

                // Act & Assert
                mockMvc.perform(post("/MesurePlant/get")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(plantInfo)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("mesure123"))
                                .andExpect(jsonPath("$[0].Soil_moisture").value(45.0));
        }

        @Test
        void testGetMesurePlant_InvalidToken() throws Exception {
                // Arrange
                plantInfo plantInfo = new plantInfo();
                plantInfo.setUser(testUser);
                plantInfo.setPlant(testPlant);

                when(mesurePlantService.getIotCaptureMesurePlant(any(plantInfo.class), anyString()))
                                .thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/MesurePlant/get")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(plantInfo)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid input or token verification failed"));
        }

        @Test
        void testGetRecommendation_Success() throws Exception {
                // Arrange
                Map<String, Object> recommendationResponse = new HashMap<>();
                recommendationResponse.put("recommendation", "Increase watering");
                recommendationResponse.put("status", "success");
                recommendationResponse.put("confidence", 0.85);

                when(mesurePlantService.getRecommendation(any(MesuresEachPlant.class)))
                                .thenReturn(recommendationResponse);

                // Act & Assert
                mockMvc.perform(post("/MesurePlant/recommend")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testMesure)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.recommendation").value("Increase watering"))
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.confidence").value(0.85));
        }

        @Test
        void testGetRecommendation_EmptyResponse() throws Exception {
                // Arrange
                Map<String, Object> emptyResponse = new HashMap<>();

                when(mesurePlantService.getRecommendation(any(MesuresEachPlant.class)))
                                .thenReturn(emptyResponse);

                // Act & Assert
                mockMvc.perform(post("/MesurePlant/recommend")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testMesure)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isMap());
        }

        @Test
        void testInsertMesurePlant_WithAllParameters() throws Exception {
                // Arrange
                PlantInput completeInput = new PlantInput();
                completeInput.setUser(testUser);
                completeInput.setPlant(testPlant);
                completeInput.setTemperature(22.0);
                completeInput.setHumidity(70.0);
                completeInput.setSoilMoisture(50.0);
                completeInput.setReferenceEvapotranspiration(4.0);
                completeInput.setEvapotranspiration(3.0);
                completeInput.setCropCoefficient(1.0);
                completeInput.setCropCoefficientStage(0.8); // Fixed: set double
                completeInput.setNitrogen(25.0);
                completeInput.setPhosphorus(18.0);
                completeInput.setPotassium(35.0);
                completeInput.setSolarRadiationGhi(550.0);
                completeInput.setWindSpeed(2.5);
                completeInput.setDaysOfPlanted(60);
                completeInput.setPH(6.8);

                List<MesuresEachPlant> mesuresList = new ArrayList<>();
                MesuresEachPlant newMesure = new MesuresEachPlant();
                newMesure.setId("mesure456");
                newMesure.setTemperature(22.0);
                newMesure.setHumidity(70.0);
                mesuresList.add(newMesure);

                when(mesurePlantService.InsertIotCapturesMesurePlant(any(PlantInput.class), anyString()))
                                .thenReturn((List) mesuresList);

                // Act & Assert
                mockMvc.perform(post("/MesurePlant/insert")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(completeInput)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].Temperature").value(22.0))
                                .andExpect(jsonPath("$[0].Humidity").value(70.0));
        }

        @Test
        void testGetMesurePlant_EmptyList() throws Exception {
                // Arrange
                plantInfo plantInfo = new plantInfo();
                plantInfo.setUser(testUser);
                plantInfo.setPlant(testPlant);

                List<MesuresEachPlant> emptyList = new ArrayList<>();

                when(mesurePlantService.getIotCaptureMesurePlant(any(plantInfo.class), anyString()))
                                .thenReturn((List) emptyList);

                // Act & Assert
                mockMvc.perform(post("/MesurePlant/get")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(plantInfo)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
        }
}
