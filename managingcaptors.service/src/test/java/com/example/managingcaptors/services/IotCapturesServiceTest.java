package com.example.managingcaptors.services;

import com.example.managingcaptors.DAO.Captures;
import com.example.managingcaptors.DAO.CaptursInputs;
import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.entities.IotCaptures;
import com.example.managingcaptors.entities.Locations;
import com.example.managingcaptors.repo.IotCaptureRepo;
import com.example.managingcaptors.repo.LocationsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IotCapturesServiceTest {

    @Mock
    private IotCaptureRepo iotCaptureRepo;

    @Mock
    private LocationsRepo locationsRepo;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private IotCapturesService iotCapturesService;

    private User testUser;
    private Locations testLocation;
    private IotCaptures testIotCapture;
    private CaptursInputs testCaptursInputs;

    @BeforeEach
    void setUp() {
        // Initialize test user with correct constructor
        testUser = new User(1L, "First", "Last", "test@example.com", "123", "pass", "loc", "caps", "token");

        // Initialize test location
        testLocation = new Locations();
        testLocation.setId("loc123");
        testLocation.setLatitude(34.0522);
        testLocation.setLongitude(-118.2437);

        // Initialize test IoT capture
        testIotCapture = new IotCaptures();
        testIotCapture.setId("iot123");
        testIotCapture.setName("Temperature Sensor");
        testIotCapture.setUnit("Celsius");
        testIotCapture.setStatus("Active");
        testIotCapture.setTimestamp("2024-01-01T10:00:00");
        testIotCapture.setUser(testUser);
        testIotCapture.setLocation(testLocation);

        // Initialize test captures inputs (uses Entity IotCaptures)
        testCaptursInputs = new CaptursInputs();
        testCaptursInputs.setCaptures(testIotCapture);
    }

    @Test
    void testCreateIotCaptures_Success() {
        // Arrange
        String token = "valid-token";
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(locationsRepo.save(any(Locations.class))).thenReturn(testLocation);
        when(iotCaptureRepo.save(any(IotCaptures.class))).thenReturn(testIotCapture);

        List<IotCaptures> expectedList = new ArrayList<>();
        expectedList.add(testIotCapture);
        when(iotCaptureRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<?> result = iotCapturesService.CreateIotCaptures(testCaptursInputs, token);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(locationsRepo, times(1)).save(any(Locations.class));
        verify(iotCaptureRepo, times(1)).save(any(IotCaptures.class));
        verify(iotCaptureRepo, times(1)).findByUser(testUser);
    }

    @Test
    void testCreateIotCaptures_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        // Act
        List<?> result = iotCapturesService.CreateIotCaptures(testCaptursInputs, token);

        // Assert
        assertNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(locationsRepo, never()).save(any(Locations.class));
        verify(iotCaptureRepo, never()).save(any(IotCaptures.class));
    }

    @Test
    void testGetAllCaptors_Success() {
        // Arrange
        String token = "valid-token";
        List<IotCaptures> expectedList = new ArrayList<>();
        expectedList.add(testIotCapture);

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(iotCaptureRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<?> result = iotCapturesService.getAllCaptors(token, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(iotCaptureRepo, times(1)).findByUser(testUser);
    }

    @Test
    void testGetAllCaptors_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        // Act
        List<?> result = iotCapturesService.getAllCaptors(token, testUser);

        // Assert
        assertNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, testUser.getEmail());
        verify(iotCaptureRepo, never()).findByUser(testUser);
    }

    @Test
    void testActivateCaptures_FromActiveToDeactivate() {
        // Arrange
        String token = "valid-token";
        String captureId = "iot123";
        testIotCapture.setStatus("Active");

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(iotCaptureRepo.findById(captureId)).thenReturn(Optional.of(testIotCapture));
        when(iotCaptureRepo.save(any(IotCaptures.class))).thenReturn(testIotCapture);

        List<IotCaptures> expectedList = new ArrayList<>();
        expectedList.add(testIotCapture);
        when(iotCaptureRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<?> result = iotCapturesService.ActivateCaptures(token, testUser, captureId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Deactivate", testIotCapture.getStatus());
        verify(iotCaptureRepo, times(1)).save(testIotCapture);
    }

    @Test
    void testActivateCaptures_FromDeactivateToActive() {
        // Arrange
        String token = "valid-token";
        String captureId = "iot123";
        testIotCapture.setStatus("Deactivate");

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(iotCaptureRepo.findById(captureId)).thenReturn(Optional.of(testIotCapture));
        when(iotCaptureRepo.save(any(IotCaptures.class))).thenReturn(testIotCapture);

        List<IotCaptures> expectedList = new ArrayList<>();
        expectedList.add(testIotCapture);
        when(iotCaptureRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<?> result = iotCapturesService.ActivateCaptures(token, testUser, captureId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Active", testIotCapture.getStatus());
        verify(iotCaptureRepo, times(1)).save(testIotCapture);
    }

    @Test
    void testActivateCaptures_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        String captureId = "iot123";
        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        // Act
        List<?> result = iotCapturesService.ActivateCaptures(token, testUser, captureId);

        // Assert
        assertNull(result);
        verify(iotCaptureRepo, never()).findById(captureId);
    }

    @Test
    void testUpdateCaptures_Success() {
        // Arrange
        String token = "valid-token";

        // Prepare expected Captures (DAO) input
        Captures captureUpdate = new Captures();
        captureUpdate.setId("iot123");
        captureUpdate.setEmail("test@example.com");
        captureUpdate.setName("Updated Sensor");
        captureUpdate.setStatus("Deactivate");

        // Prepare Locations Entity for input
        Locations locationEntity = new Locations();
        locationEntity.setId("loc123");
        locationEntity.setLatitude(40.7128);
        locationEntity.setLongitude(-74.0060);
        captureUpdate.setLocations(locationEntity);

        when(tokenService.VerifyTheToken(token, captureUpdate.getEmail())).thenReturn(true);
        when(iotCaptureRepo.findById(captureUpdate.getId())).thenReturn(Optional.of(testIotCapture));
        // Use ID from the location entity passed inside Captures
        when(locationsRepo.getLocationsById(locationEntity.getId())).thenReturn(testLocation);
        when(locationsRepo.save(any(Locations.class))).thenReturn(testLocation);
        when(iotCaptureRepo.save(any(IotCaptures.class))).thenReturn(testIotCapture);

        // Act
        IotCaptures result = iotCapturesService.UpdateCaptures(token, captureUpdate);

        // Assert
        assertNotNull(result);
        verify(tokenService, times(1)).VerifyTheToken(token, captureUpdate.getEmail());
        verify(locationsRepo, times(1)).save(any(Locations.class));
        verify(iotCaptureRepo, times(1)).save(any(IotCaptures.class));
    }

    @Test
    void testUpdateCaptures_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        Captures captureUpdate = new Captures();
        captureUpdate.setEmail("test@example.com");

        when(tokenService.VerifyTheToken(token, captureUpdate.getEmail())).thenReturn(false);

        // Act
        IotCaptures result = iotCapturesService.UpdateCaptures(token, captureUpdate);

        // Assert
        assertNull(result);
        verify(iotCaptureRepo, never()).save(any(IotCaptures.class));
    }

    @Test
    void testDeleteIot_Success() {
        // Arrange
        String token = "valid-token";
        String captureId = "iot123";

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(true);
        when(iotCaptureRepo.findById(captureId)).thenReturn(Optional.of(testIotCapture));

        List<IotCaptures> expectedList = new ArrayList<>();
        when(iotCaptureRepo.findByUser(testUser)).thenReturn(expectedList);

        // Act
        List<?> result = iotCapturesService.deleteIot(token, testUser, captureId);

        // Assert
        assertNotNull(result);
        verify(locationsRepo, times(1)).delete(testLocation);
        verify(iotCaptureRepo, times(1)).delete(testIotCapture);
        verify(iotCaptureRepo, times(1)).findByUser(testUser);
    }

    @Test
    void testDeleteIot_NullUser() {
        // Arrange
        String token = "valid-token";
        String captureId = "iot123";

        // Act
        List<?> result = iotCapturesService.deleteIot(token, null, captureId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(iotCaptureRepo, never()).delete(any(IotCaptures.class));
    }

    @Test
    void testDeleteIot_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        String captureId = "iot123";

        when(tokenService.VerifyTheToken(token, testUser.getEmail())).thenReturn(false);

        // Act
        List<?> result = iotCapturesService.deleteIot(token, testUser, captureId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(iotCaptureRepo, never()).delete(any(IotCaptures.class));
    }
}
