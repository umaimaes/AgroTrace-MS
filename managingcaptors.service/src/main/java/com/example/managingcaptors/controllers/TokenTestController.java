package com.example.managingcaptors.controllers;

import com.example.managingcaptors.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TokenTestController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/from-users-manage")
    public ResponseEntity<String> getTokenFromUsersManage() {
        String token = tokenService.CallTheToken();
        return ResponseEntity.ok("Token from users_manage.server: " + token);
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("email") String email) {
        String clientToken = authorizationHeader;
        if (clientToken != null && clientToken.startsWith("Bearer ")) {
            clientToken = clientToken.substring(7);
        }
        boolean valid = tokenService.VerifyTheToken(clientToken, email);
        return ResponseEntity.ok(valid);
    }

    @GetMapping("/api/test")
    public ResponseEntity<String> testProtected() {
        return ResponseEntity.ok("Access Granted");
    }

    @GetMapping("/api/token-test")
    public ResponseEntity<String> echoToken(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok("Hello World " + token);
    }
}
