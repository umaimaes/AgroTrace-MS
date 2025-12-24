package com.example.managingcaptors.repo;

import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.entities.MesuresEachPlant;
import com.example.managingcaptors.entities.Plant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesuresEachPlantRepo extends MongoRepository<MesuresEachPlant, String> {
    List<MesuresEachPlant> findByUserAndPlant(User user, Plant plant);

    MesuresEachPlant findTopByPlantOrderByDateTimeDesc(Plant plant);
    
    @Query("{ 'dateTime': { $gte: ?0 } }")
    List<MesuresEachPlant> findMeasurementsAfterDate(java.util.Date date);
    
    @Query(value = "{}", sort = "{ 'dateTime': -1 }")
    List<MesuresEachPlant> findLatestMeasurements();
}
