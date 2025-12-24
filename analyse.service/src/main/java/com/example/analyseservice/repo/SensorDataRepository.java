package com.example.analyseservice.repo;

import com.example.analyseservice.entities.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // Find latest sensor reading
    SensorData findTopByOrderByCreatedAtDesc();

    // Find sensor data within a time range
    List<SensorData> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Find all sensor data ordered by creation time (latest first)
    List<SensorData> findAllByOrderByCreatedAtDesc();
}
