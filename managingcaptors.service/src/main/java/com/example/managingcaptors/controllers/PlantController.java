package com.example.managingcaptors.controllers;

import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.DAO.newPlant;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.services.PlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Plant")
public class PlantController {
    @Autowired
    private PlantService service;

    @PostMapping("/get-plant")
    public ResponseEntity<?> getPlant (@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                       @RequestBody User user) {
        String clientToken = authorizationHeader;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            clientToken = authorizationHeader.substring(7);
        }

        List<?> getPlant = service.getPlants(clientToken, user);
        if (getPlant == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(getPlant);
    }

    @PostMapping("/create-Plant")
    public ResponseEntity<?> createPlant(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                         @RequestBody newPlant p) {
        String clientToken = authorizationHeader;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            clientToken = authorizationHeader.substring(7);
        }

        List<?> response = service.insertPlant(p, clientToken);

        if (response == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/update-Plant")
    public ResponseEntity<?> updatePlant (@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                          @RequestBody Plant p) {
        String clientToken = authorizationHeader;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            clientToken = authorizationHeader.substring(7);
        }

        List<?> response = service.updatePlant(p, clientToken);
        if (response == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/delete-Plant/{id}")
    public ResponseEntity<?> deletePlant (@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                          @RequestBody User user,
                                          @PathVariable String id) {
        String clientToken = authorizationHeader;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            clientToken = authorizationHeader.substring(7);
        }

        List<?> response = service.deletePlant(id, clientToken, user);
        if (response == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(response);
    }
}
