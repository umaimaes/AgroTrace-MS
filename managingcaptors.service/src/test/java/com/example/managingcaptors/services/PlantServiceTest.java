package com.example.managingcaptors.services;

import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.DAO.newPlant;
import com.example.managingcaptors.entities.Locations;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.repo.LocationsRepo;
import com.example.managingcaptors.repo.PlantRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantServiceTest {

    @Mock
    private PlantRepo plantRepo;

    @Mock
    private LocationsRepo locationsRepo;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private PlantService plantService;

    private User testUser;
    private Locations testLocation;
    private Plant testPlant;
    private newPlant testNewPlant;

    @BeforeEach
    void setUp() {
        // Initialize test user with correct constructor
        testUser = new User(1L, "First", "Last", "test@example.com", "123", "pass", "loc", "caps", "token");

        // Initialize test location
        testLocation = new Locations();
        testLocation.setId("loc123");
        testLocation.setLatitude(34.0522);
        testLocation.setLongitude(-118.2437);

        // Initialize test plant
        testPlant = new Plant();
        testPlant.setId("plant123");
        testPlant.setName("Tomato");
        testPlant.setUser(testUser);
        testPlant.setLocation(testLocation);

        // Initialize new plant DAO
        testNewPlant = new newPlant();
        testNewPlant.setName("Tomato");
        testNewPlant.setUser(testUser);
        testNewPlant.setLatitude(34.0522);
        testNewPlant.setLongitude(-118.2437);
    }

    @Test
    void testInsertPlant_Success() {
        // Arrange
        String token = "valid-token";
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(locationsRepo.save(any(Locations.class))).thenReturn(testLocation);
        when(plantRepo.save(any(Plant.class))).thenReturn(testPlant);

        List<Plant> expectedList = new ArrayList<>();
        expectedList.add(testPlant);
        when(plantRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<Plant> result = plantService.insertPlant(testNewPlant, token);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(locationsRepo, times(1)).save(any(Locations.class));
        verify(plantRepo, times(1)).save(any(Plant.class));
        verify(plantRepo, times(1)).findByUser(testUser);
    }

    @Test
    void testInsertPlant_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        // Act
        List<Plant> result = plantService.insertPlant(testNewPlant, token);

        // Assert
        assertNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(locationsRepo, never()).save(any(Locations.class));
        verify(plantRepo, never()).save(any(Plant.class));
    }

    @Test
    void testUpdatePlant_Success() {
        // Arrange
        String token = "valid-token";
        Plant updatePlant = new Plant();
        updatePlant.setId("plant123");
        updatePlant.setName("Updated Tomato");
        updatePlant.setUser(testUser);

        Locations updatedLocation = new Locations();
        updatedLocation.setId("loc123");
        updatedLocation.setLatitude(40.7128);
        updatedLocation.setLongitude(-74.0060);
        updatePlant.setLocation(updatedLocation);

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(plantRepo.findPlantById(updatePlant.getId())).thenReturn(testPlant);
        when(locationsRepo.getLocationsById(updatedLocation.getId())).thenReturn(testLocation);
        when(locationsRepo.save(any(Locations.class))).thenReturn(testLocation);
        when(plantRepo.save(any(Plant.class))).thenReturn(testPlant);

        List<Plant> expectedList = new ArrayList<>();
        expectedList.add(testPlant);
        when(plantRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<Plant> result = plantService.updatePlant(updatePlant, token);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(plantRepo, times(1)).findPlantById(updatePlant.getId());
        verify(locationsRepo, times(1)).save(any(Locations.class));
        verify(plantRepo, times(1)).save(any(Plant.class));
    }

    @Test
    void testUpdatePlant_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        Plant updatePlant = new Plant();
        updatePlant.setUser(testUser);

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        // Act
        List<Plant> result = plantService.updatePlant(updatePlant, token);

        // Assert
        assertNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(plantRepo, never()).save(any(Plant.class));
    }

    @Test
    void testUpdatePlant_PlantNotFound() {
        // Arrange
        String token = "valid-token";
        Plant updatePlant = new Plant();
        updatePlant.setId("non-existent");
        updatePlant.setUser(testUser);

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(plantRepo.findPlantById(updatePlant.getId())).thenReturn(null);

        // Act
        List<Plant> result = plantService.updatePlant(updatePlant, token);

        // Assert
        assertNull(result);
        verify(plantRepo, never()).save(any(Plant.class));
    }

    @Test
    void testUpdatePlant_LocationNotFound() {
        // Arrange
        String token = "valid-token";
        Plant updatePlant = new Plant();
        updatePlant.setId("plant123");
        updatePlant.setUser(testUser);

        Locations updatedLocation = new Locations();
        updatedLocation.setId("non-existent-loc");
        updatePlant.setLocation(updatedLocation);

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(plantRepo.findPlantById(updatePlant.getId())).thenReturn(testPlant);
        when(locationsRepo.getLocationsById(updatedLocation.getId())).thenReturn(null);

        // Act
        List<Plant> result = plantService.updatePlant(updatePlant, token);

        // Assert
        assertNull(result);
        verify(plantRepo, never()).save(any(Plant.class));
    }

    @Test
    void testDeletePlant_Success() {
        // Arrange
        String token = "valid-token";
        String plantId = "plant123";

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(plantRepo.findPlantById(plantId)).thenReturn(testPlant);

        List<Plant> expectedList = new ArrayList<>();
        when(plantRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<Plant> result = plantService.deletePlant(plantId, token, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(plantRepo, times(1)).findPlantById(plantId);
        verify(plantRepo, times(1)).delete(testPlant);
        verify(plantRepo, times(1)).findByUser(testUser);
    }

    @Test
    void testDeletePlant_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        String plantId = "plant123";

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        // Act
        List<Plant> result = plantService.deletePlant(plantId, token, testUser);

        // Assert
        assertNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(plantRepo, never()).delete(any(Plant.class));
    }

    @Test
    void testDeletePlant_PlantNotFound() {
        // Arrange
        String token = "valid-token";
        String plantId = "non-existent";

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(plantRepo.findPlantById(plantId)).thenReturn(null);

        // Act
        List<Plant> result = plantService.deletePlant(plantId, token, testUser);

        // Assert
        assertNull(result);
        verify(plantRepo, never()).delete(any(Plant.class));
    }

    @Test
    void testGetPlants_Success() {
        // Arrange
        String token = "valid-token";
        List<Plant> expectedList = new ArrayList<>();
        expectedList.add(testPlant);

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(plantRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<Plant> result = plantService.getPlants(token, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(plantRepo, times(1)).findByUser(testUser);
    }

    @Test
    void testGetPlants_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        // Act
        List<Plant> result = plantService.getPlants(token, testUser);

        // Assert
        assertNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(plantRepo, never()).findByUser(testUser);
    }

    @Test
    void testGetPlants_EmptyList() {
        // Arrange
        String token = "valid-token";
        List<Plant> emptyList = new ArrayList<>();

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(plantRepo.findByUser(testUser)).thenReturn(emptyList);

        // Act
        List<Plant> result = plantService.getPlants(token, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(plantRepo, times(1)).findByUser(testUser);
    }
}
