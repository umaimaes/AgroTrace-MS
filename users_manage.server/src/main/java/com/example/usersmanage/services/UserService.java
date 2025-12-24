package com.example.usersmanage.services;

import com.example.usersmanage.DOA.RegisterRequest;
import com.example.usersmanage.DOA.UserInfo;
import com.example.usersmanage.config.JwtUtil;
import com.example.usersmanage.config.TokenBlacklistService;
import com.example.usersmanage.entities.Users;
import com.example.usersmanage.repositories.UsersSqlRepo;
import lombok.Getter;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UsersSqlRepo repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SmtpService smtp;

    @Getter
    private UserInfo info;

    private final Map<String, Users> resetTokenStorage = new ConcurrentHashMap<>();

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public boolean register(RegisterRequest r) {
        if (r == null)
            return false;

        if (repo.existsByEmail(r.getEmail())) {
            return false;
        }

        Users newUser = new Users();

        newUser.setFirstname(r.getFirstname());
        newUser.setLastname(r.getLastname());
        newUser.setEmail(r.getEmail());
        newUser.setTel(r.getPhone());
        newUser.setPassword(passwordEncoder.encode(r.getPassword()));

        repo.save(newUser);
        return true;
    }

    public UserInfo login(String email, String password) {
        if (email == null || password == null)
            return null;

        Users user = repo.findFirstByEmail(email).orElse(null);

        if (user == null)
            return null;

        if (!passwordEncoder.matches(password, user.getPassword()))
            return null;

        info = new UserInfo(user, jwtUtil.generateToken(email));

        return info;
    }

    public void logOut(String token) {
        if (token == null)
            return;
        tokenBlacklistService.blacklist(token);
    }

    public String VerificationCode(String email) {
        System.out.println("bekvqhlkwJ.BF;VKWFH gj.bcj;l    wg;vbc;cbjkbalkjemail: " + email);

        Users user = repo.findFirstByEmail(email).orElse(null);
        if (user == null)
            return null;

        System.out.println("user: " + user);

        SecureRandom random = new SecureRandom();
        String code = String.format("%06d", random.nextInt(1_000_000));
        resetTokenStorage.put(code, user);

        System.out.println("code: " + code);

        new Thread(() -> {
            try {
                TimeUnit.MINUTES.sleep(10);
                resetTokenStorage.remove(code);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        String emailBody = String.format("""
                Hello %s %s, \n \n \n

                We received a request to reset the password of your Agro account. \n
                Please use the verification code below to continue: \n \n

                🔐 Verification Code: %s \n \n

                This code is valid for the next 10 minutes. Please do not share it with anyone. \n

                If you did not request a password reset, you can safely ignore this email. \n \n \n

                — The Agro Team
                """, capitalize(user.getFirstname()), capitalize(user.getLastname()), code);

        try {
            smtp.SendEmail(email, "Agro: Password request", emailBody);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + email + ": " + e.getMessage());
            e.printStackTrace();
        }

        return code;
    }

    public boolean verifyCode(String email, String code) {
        Users user = repo.findFirstByEmail(email).orElse(null);
        if (user == null)
            return false;

        Users storedUser = resetTokenStorage.get(code);
        if (storedUser == null)
            return false;

        return storedUser.getEmail().equals(user.getEmail());
    }

    public boolean resetPassword(String code, String password) {
        Users user = resetTokenStorage.get(code);

        if (user == null)
            return false;
        user.setPassword(passwordEncoder.encode(password));

        repo.save(user);
        return true;
    }
}
