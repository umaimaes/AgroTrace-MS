package com.example.analyseservice.dto;

import lombok.Data;

@Data
public class SensorDataResponse {
    private String status;
    private String message;
    private SensorDataDTO data;
}
