package com.example.usersmanage.Controllers;

import com.example.usersmanage.DOA.RegisterRequest;
import com.example.usersmanage.DOA.UserInfo;
import com.example.usersmanage.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UsersController {

    @Autowired
    private UserService service;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(service.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<UserInfo> login(@RequestParam String email, @RequestParam String password) {
        UserInfo userInfo = service.login(email, password);
        if (userInfo == null) {
            return ResponseEntity.status(401).body(null);
        }
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(false);
        }

        String jwtToken = token.substring(7);
        service.logOut(jwtToken);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestParam String email) {
        String code = service.VerificationCode(email);
        if (code == null) {
            return ResponseEntity.status(400).body("User with this email not found.");
        }
        return ResponseEntity.ok(code);
    }

    @GetMapping("/verification-code/{email}")
    public ResponseEntity<?> verifyCode(@PathVariable String email, @RequestParam String code) {
        return ResponseEntity.ok(service.verifyCode(email, code));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String code, @RequestParam String password) {
        return ResponseEntity.ok(service.resetPassword(code, password));
    }

    @GetMapping("/get-token-info")
    public ResponseEntity<?> getToken() {
        return ResponseEntity.ok(service.getInfo());
    }
}
