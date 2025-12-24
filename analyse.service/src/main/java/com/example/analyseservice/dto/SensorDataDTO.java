package com.example.analyseservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SensorDataDTO {

    @JsonProperty("soil_humidity_A4")
    private String soilHumidityA4;

    @JsonProperty("soil_humidity_A2")
    private String soilHumidityA2;

    @JsonProperty("gas_A5")
    private String gasA5;

    @JsonProperty("rain_A3")
    private String rainA3;

    @JsonProperty("temperature")
    private String temperature;

    @JsonProperty("air_humidity")
    private String airHumidity;

    @JsonProperty("timestamp")
    private String timestamp;
}
