package com.example.analyseservice.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Tests")
class DTOTest {

    @Test
    @DisplayName("SensorDataDTO - Getter and Setter Test")
    void testSensorDataDTO() {
        SensorDataDTO dto = new SensorDataDTO();
        dto.setSoilHumidityA4("512");
        dto.setSoilHumidityA2("487");
        dto.setGasA5("234");
        dto.setRainA3("100");
        dto.setTemperature("25.4");
        dto.setAirHumidity("65.2");
        dto.setTimestamp("2025-12-21 15:00:00");

        assertEquals("512", dto.getSoilHumidityA4());
        assertEquals("487", dto.getSoilHumidityA2());
        assertEquals("234", dto.getGasA5());
        assertEquals("100", dto.getRainA3());
        assertEquals("25.4", dto.getTemperature());
        assertEquals("65.2", dto.getAirHumidity());
        assertEquals("2025-12-21 15:00:00", dto.getTimestamp());

        assertNotNull(dto.toString());
        assertEquals(dto, dto);
        assertNotEquals(dto, new Object());

        SensorDataDTO dto2 = new SensorDataDTO();
        dto2.setSoilHumidityA4("512");
        dto2.setSoilHumidityA2("487");
        dto2.setGasA5("234");
        dto2.setRainA3("100");
        dto2.setTemperature("25.4");
        dto2.setAirHumidity("65.2");
        dto2.setTimestamp("2025-12-21 15:00:00");

        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("SensorDataResponse - Getter and Setter Test")
    void testSensorDataResponse() {
        SensorDataResponse response = new SensorDataResponse();
        response.setStatus("success");
        response.setMessage("test message");

        SensorDataDTO dto = new SensorDataDTO();
        dto.setTemperature("25.4");
        response.setData(dto);

        assertEquals("success", response.getStatus());
        assertEquals("test message", response.getMessage());
        assertEquals(dto, response.getData());

        assertNotNull(response.toString());
        assertEquals(response, response);

        SensorDataResponse response2 = new SensorDataResponse();
        response2.setStatus("success");
        response2.setMessage("test message");
        response2.setData(dto);

        assertEquals(response, response2);
        assertEquals(response.hashCode(), response2.hashCode());
    }
}
