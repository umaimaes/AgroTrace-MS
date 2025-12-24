package com.example.managingcaptors.controllers;

import com.example.managingcaptors.DAO.PlantInput;
import com.example.managingcaptors.DAO.plantInfo;
import com.example.managingcaptors.services.MesurePlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/MesurePlant")
public class MesurePlantController {
    @Autowired
    private MesurePlantService service;

    @PostMapping("/insert")
    public ResponseEntity<?> insertIotCapturesMesurePlant(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody PlantInput p) {
        String clientToken = authorizationHeader;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            clientToken = authorizationHeader.substring(7);
        }

        List<?> response = service.InsertIotCapturesMesurePlant(p, clientToken);

        if (response == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/get")
    public ResponseEntity<?> getIotCaptureMesurePlant(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody plantInfo p) {
        String clientToken = authorizationHeader;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            clientToken = authorizationHeader.substring(7);
        }

        List<?> response = service.getIotCaptureMesurePlant(p, clientToken);

        if (response == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/recommend")
    public ResponseEntity<?> getRecommendation(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody com.example.managingcaptors.entities.MesuresEachPlant p) {

        Object response = service.getRecommendation(p);

        if (response == null) {
            return ResponseEntity.badRequest().body("AI Service unavailable or invalid input");
        }

        return ResponseEntity.ok(response);
    }
}
