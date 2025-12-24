package com.example.managingcaptors.services;

import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.DAO.newPlant;
import com.example.managingcaptors.entities.Locations;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.repo.LocationsRepo;
import com.example.managingcaptors.repo.PlantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantService {

    @Autowired
    private PlantRepo repo;

    @Autowired
    private LocationsRepo LocationsRepo;

    @Autowired
    private TokenService tokenService;

    public List<Plant> insertPlant(newPlant p, String token) {
        System.out.println("plant: " + p);
        if (!tokenService.VerifyTheToken(token, p.getUser().getEmail())) return null;

        System.out.println("valid token");

        Locations locations = new Locations();
        locations.setLongitude(p.getLongitude());
        locations.setLatitude(p.getLatitude());
        LocationsRepo.save(locations);

        System.out.println("location: " + locations);

        Plant newPlant = new Plant();
        newPlant.setUser(p.getUser());
        newPlant.setName(p.getName());
        newPlant.setLocation(locations);
        repo.save(newPlant);

        System.out.println("plantadded: " + newPlant.toString());

        return repo.findByUser(p.getUser());
    }

    public List<Plant> updatePlant(Plant p, String token) {
        if (!tokenService.VerifyTheToken(token, p.getUser().getEmail())) return null;

        Plant plant = repo.findPlantById(p.getId());
        if (plant == null) return null;

        Locations location = LocationsRepo.getLocationsById(p.getLocation().getId());
        if (location == null) return null;

        location.setLatitude(p.getLocation().getLatitude());
        location.setLongitude(p.getLocation().getLongitude());
        LocationsRepo.save(location);

        plant.setName(p.getName());
        plant.setLocation(location);
        repo.save(plant);

        return repo.findByUser(p.getUser());
    }

    public List<Plant> deletePlant(String id, String token, User user) {
        if (!tokenService.VerifyTheToken(token, user.getEmail())) return null;

        Plant plant = repo.findPlantById(id);
        if (plant == null) return null;

        repo.delete(plant);

        return repo.findByUser(user);
    }

    public List<Plant> getPlants(String token, User user) {
        if (!tokenService.VerifyTheToken(token, user.getEmail())) return null;

        return repo.findByUser(user);
    }
}
