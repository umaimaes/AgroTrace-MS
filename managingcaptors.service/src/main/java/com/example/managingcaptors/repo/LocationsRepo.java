package com.example.managingcaptors.repo;

import com.example.managingcaptors.entities.Locations;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationsRepo extends MongoRepository<Locations, String> {
    Locations getLocationsById(String id);
}
