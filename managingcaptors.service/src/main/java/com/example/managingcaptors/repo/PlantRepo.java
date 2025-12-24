package com.example.managingcaptors.repo;

import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.entities.Plant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PlantRepo extends MongoRepository<Plant, String> {
    Plant findByUserAndName(User u, String name);
    Plant findPlantById(String id);
    List<Plant> findByUser(User user);
}
