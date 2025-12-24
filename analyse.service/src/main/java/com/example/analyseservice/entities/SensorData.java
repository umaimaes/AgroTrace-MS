package com.example.analyseservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "soil_humidity_a4")
    private String soilHumidityA4;

    @Column(name = "soil_humidity_a2")
    private String soilHumidityA2;

    @Column(name = "gas_a5")
    private String gasA5;

    @Column(name = "rain_a3")
    private String rainA3;

    @Column(name = "temperature")
    private String temperature;

    @Column(name = "air_humidity")
    private String airHumidity;

    @Column(name = "sensor_timestamp")
    private String sensorTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
