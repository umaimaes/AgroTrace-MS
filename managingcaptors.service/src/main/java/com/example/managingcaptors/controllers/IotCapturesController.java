package com.example.managingcaptors.controllers;

import com.example.managingcaptors.DAO.Captures;
import com.example.managingcaptors.DAO.CaptursInputs;
import com.example.managingcaptors.DAO.Info;
import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.entities.IotCaptures;
import com.example.managingcaptors.services.IotCapturesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/IotCaptures")
public class IotCapturesController {
    @Autowired
    private IotCapturesService IoTservice;

    @PostMapping("/Add-Capture")
    public ResponseEntity<?> Add_Capture(@RequestBody CaptursInputs inputs, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        String clientToken = authorizationHeader;
        if (clientToken != null && clientToken.startsWith("Bearer ")) {
            clientToken = clientToken.substring(7);
        }

        List<?> captures = IoTservice.CreateIotCaptures(inputs, clientToken);
        if (captures == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }
        return ResponseEntity.ok(captures);
    }

    @PostMapping("/Get-Capture")
    public ResponseEntity<?> Get_Capture(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody User user) {

        String clientToken = authorizationHeader;
        if (clientToken != null && clientToken.startsWith("Bearer ")) {
            clientToken = clientToken.substring(7);
        }

        List<?> captures = IoTservice.getAllCaptors(clientToken, user);
        if (captures == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }
        return ResponseEntity.ok(captures);
    }

    @PostMapping("/Activate-Deactivate")
    public ResponseEntity<?> Activate_Deactivate(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Info info) {
        String clientToken = authorizationHeader;
        if (clientToken != null && clientToken.startsWith("Bearer ")) {
            clientToken = clientToken.substring(7);
        }

        List<?> captures = IoTservice.ActivateCaptures(clientToken, info.getU(), info.getId_cap());
        if (captures == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(captures);
    }

    @PostMapping("/Update-Captor")
    public ResponseEntity<?> Updadte_Captor(@RequestBody Captures cap, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String clientToken = authorizationHeader;
        if (clientToken != null && clientToken.startsWith("Bearer ")) {
            clientToken = clientToken.substring(7);
        }

        IotCaptures result = IoTservice.UpdateCaptures(clientToken, cap);
        if (result == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/delete-Captor/{idIoT}")
    public ResponseEntity<?> deleteCaptor(@PathVariable String idIoT,
                                          @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                          @RequestBody User user) {

        String clientToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            clientToken = authorizationHeader.substring(7);
        }

        List<?> listIot = IoTservice.deleteIot(clientToken, user,idIoT);
        if (listIot == null) {
            return ResponseEntity.badRequest().body("Invalid input or token verification failed");
        }

        return ResponseEntity.ok(listIot);
    }
}
