package com.example.analyseservice.repo;

import com.example.analyseservice.entities.SensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Sensor Data Repository Tests")
class SensorDataRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    private SensorData sensorData1;
    private SensorData sensorData2;
    private SensorData sensorData3;

    @BeforeEach
    void setUp() {
        // Create test data
        sensorData1 = new SensorData();
        sensorData1.setSoilHumidityA4("512");
        sensorData1.setSoilHumidityA2("487");
        sensorData1.setGasA5("234");
        sensorData1.setRainA3("100");
        sensorData1.setTemperature("25.4");
        sensorData1.setAirHumidity("65.2");
        sensorData1.setSensorTimestamp("2025-12-21 15:00:00");
        sensorData1.setCreatedAt(LocalDateTime.now().minusMinutes(2));

        sensorData2 = new SensorData();
        sensorData2.setSoilHumidityA4("515");
        sensorData2.setSoilHumidityA2("490");
        sensorData2.setGasA5("240");
        sensorData2.setRainA3("105");
        sensorData2.setTemperature("25.8");
        sensorData2.setAirHumidity("66.0");
        sensorData2.setSensorTimestamp("2025-12-21 15:01:00");
        sensorData2.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        sensorData3 = new SensorData();
        sensorData3.setSoilHumidityA4("520");
        sensorData3.setSoilHumidityA2("495");
        sensorData3.setGasA5("245");
        sensorData3.setRainA3("110");
        sensorData3.setTemperature("26.1");
        sensorData3.setAirHumidity("67.3");
        sensorData3.setSensorTimestamp("2025-12-21 15:02:00");
        sensorData3.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Save Sensor Data - Success")
    void testSaveSensorData_Success() {
        // When
        SensorData saved = sensorDataRepository.save(sensorData1);

        // Then
        assertNotNull(saved.getId());
        assertEquals("512", saved.getSoilHumidityA4());
        assertEquals("25.4", saved.getTemperature());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Find Top By Order By Created At Desc - Success")
    void testFindTopByOrderByCreatedAtDesc_Success() {
        // Given
        entityManager.persist(sensorData1);
        entityManager.persist(sensorData2);
        entityManager.persist(sensorData3);
        entityManager.flush();

        // When
        SensorData latest = sensorDataRepository.findTopByOrderByCreatedAtDesc();

        // Then
        assertNotNull(latest);
        assertEquals("520", latest.getSoilHumidityA4());
        assertEquals("26.1", latest.getTemperature());
    }

    @Test
    @DisplayName("Find Top By Order By Created At Desc - Empty Database")
    void testFindTopByOrderByCreatedAtDesc_EmptyDatabase() {
        // When
        SensorData latest = sensorDataRepository.findTopByOrderByCreatedAtDesc();

        // Then
        assertNull(latest);
    }

    @Test
    @DisplayName("Find By Created At Between - Success")
    void testFindByCreatedAtBetween_Success() {
        // Given
        entityManager.persist(sensorData1);
        entityManager.persist(sensorData2);
        entityManager.persist(sensorData3);
        entityManager.flush();

        LocalDateTime start = LocalDateTime.now().minusMinutes(3);
        LocalDateTime end = LocalDateTime.now().minusSeconds(30);

        // When
        List<SensorData> results = sensorDataRepository.findByCreatedAtBetween(start, end);

        // Then
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Find By Created At Between - No Results")
    void testFindByCreatedAtBetween_NoResults() {
        // Given
        entityManager.persist(sensorData1);
        entityManager.flush();

        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now().minusHours(1);

        // When
        List<SensorData> results = sensorDataRepository.findByCreatedAtBetween(start, end);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Find All By Order By Created At Desc - Success")
    void testFindAllByOrderByCreatedAtDesc_Success() {
        // Given
        entityManager.persist(sensorData1);
        entityManager.persist(sensorData2);
        entityManager.persist(sensorData3);
        entityManager.flush();

        // When
        List<SensorData> results = sensorDataRepository.findAllByOrderByCreatedAtDesc();

        // Then
        assertEquals(3, results.size());
        assertEquals("520", results.get(0).getSoilHumidityA4()); // Most recent
        assertEquals("515", results.get(1).getSoilHumidityA4());
        assertEquals("512", results.get(2).getSoilHumidityA4()); // Oldest
    }

    @Test
    @DisplayName("Find All By Order By Created At Desc - Empty Database")
    void testFindAllByOrderByCreatedAtDesc_EmptyDatabase() {
        // When
        List<SensorData> results = sensorDataRepository.findAllByOrderByCreatedAtDesc();

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Count - With Data")
    void testCount_WithData() {
        // Given
        entityManager.persist(sensorData1);
        entityManager.persist(sensorData2);
        entityManager.persist(sensorData3);
        entityManager.flush();

        // When
        long count = sensorDataRepository.count();

        // Then
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Count - Empty Database")
    void testCount_EmptyDatabase() {
        // When
        long count = sensorDataRepository.count();

        // Then
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Delete Sensor Data - Success")
    void testDeleteSensorData_Success() {
        // Given
        SensorData saved = entityManager.persist(sensorData1);
        entityManager.flush();
        Long id = saved.getId();

        // When
        sensorDataRepository.deleteById(id);
        entityManager.flush();

        // Then
        assertFalse(sensorDataRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("Update Sensor Data - Success")
    void testUpdateSensorData_Success() {
        // Given
        SensorData saved = entityManager.persist(sensorData1);
        entityManager.flush();

        // When
        saved.setTemperature("30.0");
        SensorData updated = sensorDataRepository.save(saved);
        entityManager.flush();

        // Then
        assertEquals("30.0", updated.getTemperature());
        assertEquals(saved.getId(), updated.getId());
    }
}
