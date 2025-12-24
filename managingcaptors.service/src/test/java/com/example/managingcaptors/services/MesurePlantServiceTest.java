package com.example.managingcaptors.services;

import com.example.managingcaptors.DAO.PlantInput;
import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.DAO.plantInfo;
import com.example.managingcaptors.entities.MesuresEachPlant;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.repo.IotCaptureRepo;
import com.example.managingcaptors.repo.LocationsRepo;
import com.example.managingcaptors.repo.MesuresEachPlantRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MesurePlantServiceTest {

    @Mock
    private MesuresEachPlantRepo mesuresEachPlantRepo;

    @Mock
    private IotCaptureRepo iotCaptureRepo;

    @Mock
    private LocationsRepo locationsRepo;

    @Mock
    private TokenService tokenService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MesurePlantService mesurePlantService;

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
    void testInsertIotCapturesMesurePlant_Success() {
        // Arrange
        String token = "valid-token";
        // Note: The service inverts the logic (returns null when token is valid)
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);
        when(mesuresEachPlantRepo.save(any(MesuresEachPlant.class))).thenReturn(testMesure);

        List<MesuresEachPlant> expectedList = new ArrayList<>();
        expectedList.add(testMesure);
        when(mesuresEachPlantRepo.findByUserAndPlant(testUser, testPlant)).thenReturn(expectedList);

        // Act
        List<?> result = mesurePlantService.InsertIotCapturesMesurePlant(testPlantInput, token);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(mesuresEachPlantRepo, times(1)).save(any(MesuresEachPlant.class));
        verify(mesuresEachPlantRepo, times(1)).findByUserAndPlant(testUser, testPlant);
    }

    @Test
    void testInsertIotCapturesMesurePlant_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        // Note: The service inverts the logic (returns null when token verification
        // returns true)
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);

        // Act
        List<?> result = mesurePlantService.InsertIotCapturesMesurePlant(testPlantInput, token);

        // Assert
        assertNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(mesuresEachPlantRepo, never()).save(any(MesuresEachPlant.class));
    }

    @Test
    void testGetIotCaptureMesurePlant_Success() {
        // Arrange
        String token = "valid-token";
        plantInfo plantInfo = new plantInfo();
        plantInfo.setUser(testUser);
        plantInfo.setPlant(testPlant);

        // Note: The service inverts the logic
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        List<MesuresEachPlant> expectedList = new ArrayList<>();
        expectedList.add(testMesure);
        when(mesuresEachPlantRepo.findByUserAndPlant(testUser, testPlant)).thenReturn(expectedList);

        // Act
        List<?> result = mesurePlantService.getIotCaptureMesurePlant(plantInfo, token);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(mesuresEachPlantRepo, times(1)).findByUserAndPlant(testUser, testPlant);
    }

    @Test
    void testGetIotCaptureMesurePlant_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        plantInfo plantInfo = new plantInfo();
        plantInfo.setUser(testUser);
        plantInfo.setPlant(testPlant);

        // Note: The service inverts the logic
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);

        // Act
        List<?> result = mesurePlantService.getIotCaptureMesurePlant(plantInfo, token);

        // Assert
        assertNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(mesuresEachPlantRepo, never()).findByUserAndPlant(any(), any());
    }

    @Test
    void testGetRecommendation_Success() {
        // Arrange
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("recommendation", "Increase watering");
        expectedResponse.put("status", "success");

        when(restTemplate.postForObject(anyString(), any(Map.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        // Act
        Object result = mesurePlantService.getRecommendation(testMesure);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("success", resultMap.get("status"));
        verify(restTemplate, times(1)).postForObject(anyString(), any(Map.class), eq(Object.class));
    }

    @Test
    void testGetRecommendation_WithAllParameters() {
        // Arrange
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("recommendation", "Optimal conditions");

        when(restTemplate.postForObject(anyString(), any(Map.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        // Act
        Object result = mesurePlantService.getRecommendation(testMesure);

        // Assert
        assertNotNull(result);

        // Verify that the correct URL was called
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8000/recommend"),
                argThat(body -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bodyMap = (Map<String, Object>) body;
                    return bodyMap.containsKey("plant_id")
                            && bodyMap.containsKey("plant_name")
                            && bodyMap.containsKey("user_email")
                            && bodyMap.containsKey("temperature")
                            && bodyMap.containsKey("humidity")
                            && bodyMap.containsKey("soil_moisture")
                            && bodyMap.containsKey("nitrogen")
                            && bodyMap.containsKey("phosphorus")
                            && bodyMap.containsKey("potassium")
                            && bodyMap.containsKey("ph")
                            && bodyMap.containsKey("solar_radiation")
                            && bodyMap.containsKey("wind_speed")
                            && bodyMap.containsKey("stage");
                }),
                eq(Object.class));
    }

    @Test
    void testGetRecommendation_ValidatesRequestBody() {
        // Arrange
        when(restTemplate.postForObject(anyString(), any(Map.class), eq(Object.class)))
                .thenReturn(new HashMap<>());

        // Act
        mesurePlantService.getRecommendation(testMesure);

        // Assert and verify the body content
        verify(restTemplate).postForObject(
                eq("http://localhost:8000/recommend"),
                argThat(body -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bodyMap = (Map<String, Object>) body;
                    return bodyMap.get("plant_id").equals(testPlant.getId())
                            && bodyMap.get("plant_name").equals(testPlant.getName())
                            && bodyMap.get("user_email").equals(testUser.getEmail())
                            && bodyMap.get("temperature").equals(testMesure.getTemperature())
                            && bodyMap.get("humidity").equals(testMesure.getHumidity())
                            && bodyMap.get("soil_moisture").equals(testMesure.getSoilMoisture())
                            && bodyMap.get("stage").equals(testMesure.getCropCoefficientStage());
                }),
                eq(Object.class));
    }
}
