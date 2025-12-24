package com.example.managingcaptors;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Value("${jwt.secret}")
        private String secretKey;

        @Test
        public void accessProtectedEndpoint_WithValidToken_ShouldReturn200() throws Exception {
                String token = Jwts.builder()
                                .setSubject("testuser@example.com")
                                .setIssuedAt(new Date())
                                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                                .signWith(SignatureAlgorithm.HS256, secretKey)
                                .compact();

                mockMvc.perform(get("/api/test")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Access Granted"));
        }

        @Test
        public void accessProtectedEndpoint_WithoutToken_ShouldReturn403() throws Exception {
                mockMvc.perform(get("/api/test"))
                                .andExpect(status().isForbidden());
        }

        @Test
        public void accessProtectedEndpoint_WithInvalidToken_ShouldReturn403() throws Exception {
                String token = "invalid_token";
                mockMvc.perform(get("/api/test")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        public void echoToken_ShouldReturnToken() throws Exception {
                String token = Jwts.builder()
                                .setSubject("testuser@example.com")
                                .setIssuedAt(new Date())
                                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                                .signWith(SignatureAlgorithm.HS256, secretKey)
                                .compact();

                mockMvc.perform(get("/api/token-test")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Hello World Bearer " + token));
        }
}
