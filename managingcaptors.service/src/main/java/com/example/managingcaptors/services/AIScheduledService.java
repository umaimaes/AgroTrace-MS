package com.example.managingcaptors.services;

import com.example.managingcaptors.entities.MesuresEachPlant;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.repo.MesuresEachPlantRepo;
import com.example.managingcaptors.repo.PlantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIScheduledService {

    @Autowired
    private PlantRepo plantRepo;

    @Autowired
    private MesuresEachPlantRepo mesuresEachPlantRepo;

    @Autowired
    private MesurePlantService mesurePlantService;

    // Runs at 8:00 AM and 8:00 PM every day
    @Scheduled(cron = "0 0 8,20 * * *")
    public void sendMeasurementsToAI() {
        System.out.println("Starting scheduled task: Sending plant measurements to AI Service...");

        List<Plant> allPlants = plantRepo.findAll();

        for (Plant plant : allPlants) {
            try {
                MesuresEachPlant latestMeasurement = mesuresEachPlantRepo.findTopByPlantOrderByDateTimeDesc(plant);

                if (latestMeasurement != null) {
                    System.out.println("Sending data for plant: " + plant.getName());
                    mesurePlantService.getRecommendation(latestMeasurement);
                } else {
                    System.out.println("No measurements found for plant: " + plant.getName());
                }
            } catch (Exception e) {
                System.err.println("Error processing plant " + plant.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("Scheduled task completed.");
    }
}
