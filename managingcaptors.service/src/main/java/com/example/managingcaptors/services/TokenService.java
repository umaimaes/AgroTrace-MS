package com.example.managingcaptors.services;

import com.example.managingcaptors.DAO.User;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TokenService {

    @org.springframework.beans.factory.annotation.Autowired
    private RestTemplate restTemplate;

    public User getUser(User u) {
        return u;
    }

    public String CallTheToken() {
        // RestTemplate used from field
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:8081/user/get-token-info",
                HttpMethod.GET,
                null,
                Map.class);
        Map body = response.getBody();
        if (body == null) {
            return null;
        }
        Object tokenObj = body.get("token");
        return tokenObj instanceof String ? (String) tokenObj : null;
    }

    public boolean VerifyTheToken(String clientToken, String clientEmail) {
        // RestTemplate used from field
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:8081/user/get-token-info",
                HttpMethod.GET,
                null,
                Map.class);
        Map body = response.getBody();
        if (body == null) {
            return false;
        }

        Object serverTokenObj = body.get("token");
        Object userObj = body.get("user");

        if (!(serverTokenObj instanceof String) || !(userObj instanceof Map)) {
            return false;
        }

        String serverToken = (String) serverTokenObj;
        Map userMap = (Map) userObj;

        Object serverEmailObj = userMap.get("email");
        if (!(serverEmailObj instanceof String)) {
            return false;
        }

        String serverEmail = (String) serverEmailObj;

        System.out.println("serverToken = " + serverToken);
        System.out.println("serverEmail = " + serverEmail);
        System.out.println("clientToken = " + clientToken);
        System.out.println("clientEmail = " + clientEmail);

        return clientToken != null
                && clientEmail != null
                && clientToken.equals(serverToken)
                && clientEmail.equals(serverEmail);
    }
}
