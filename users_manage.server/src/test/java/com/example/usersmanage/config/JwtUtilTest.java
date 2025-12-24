package com.example.usersmanage.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Utility Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secretKey = "TestSecretKeyThatIsLongEnoughToBeSecure1234567890";
    private final Long expiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    @DisplayName("Generate Token - Should return non-null token")
    void testGenerateToken() {
        String token = jwtUtil.generateToken("test@example.com");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("Extract Email - Should return correct email")
    void testExtractEmail() {
        String token = jwtUtil.generateToken("test@example.com");
        String email = jwtUtil.extractEmail(token);
        assertEquals("test@example.com", email);
    }

    @Test
    @DisplayName("Is Token Expired - Should return false for new token")
    void testIsTokenExpired() {
        String token = jwtUtil.generateToken("test@example.com");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("Validate Token - Should return true for valid token and matching email")
    void testValidateToken_Valid() {
        String token = jwtUtil.generateToken("test@example.com");
        assertTrue(jwtUtil.validateToken(token, "test@example.com"));
    }

    @Test
    @DisplayName("Validate Token - Should return false for wrong email")
    void testValidateToken_WrongEmail() {
        String token = jwtUtil.generateToken("test@example.com");
        assertFalse(jwtUtil.validateToken(token, "wrong@example.com"));
    }

    @Test
    @DisplayName("Validate Token - Should return false for invalid token")
    void testValidateToken_Invalid() {
        assertFalse(jwtUtil.validateToken("invalidToken", "test@example.com"));
    }
}
