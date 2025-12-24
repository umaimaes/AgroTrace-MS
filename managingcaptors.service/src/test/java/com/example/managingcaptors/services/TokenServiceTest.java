package com.example.managingcaptors.services;

import com.example.managingcaptors.DAO.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TokenService tokenService;

    private User testUser;
    private Map<String, Object> validResponseBody;

    @BeforeEach
    void setUp() {
        // Initialize test user with correct constructor
        testUser = new User(1L, "First", "Last", "test@example.com", "123", "pass", "loc", "caps", "token");

        // Initialize valid response
        validResponseBody = new HashMap<>();
        validResponseBody.put("token", "valid-server-token");

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", "test@example.com");
        userMap.put("name", "Test User");

        validResponseBody.put("user", userMap);
    }

    @Test
    void testGetUser_ReturnsUser() {
        // Act
        User result = tokenService.getUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void testGetUser_NullUser() {
        // Act
        User result = tokenService.getUser(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testCallTheToken_Success() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(validResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/user/get-token-info"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        String result = tokenService.CallTheToken();

        // Assert
        assertNotNull(result);
        assertEquals("valid-server-token", result);
        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:8081/user/get-token-info"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class));
    }

    @Test
    void testCallTheToken_NullResponseBody() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        String result = tokenService.CallTheToken();

        // Assert
        assertNull(result);
    }

    @Test
    void testCallTheToken_TokenNotString() {
        // Arrange
        Map<String, Object> invalidResponseBody = new HashMap<>();
        invalidResponseBody.put("token", 12345); // Token as integer instead of string

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(invalidResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        String result = tokenService.CallTheToken();

        // Assert
        assertNull(result);
    }

    @Test
    void testCallTheToken_NoTokenField() {
        // Arrange
        Map<String, Object> invalidResponseBody = new HashMap<>();
        invalidResponseBody.put("data", "some-value");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(invalidResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        String result = tokenService.CallTheToken();

        // Assert
        assertNull(result);
    }

    @Test
    void testVerifyTheToken_Success() {
        // Arrange
        String clientToken = "valid-server-token";
        String clientEmail = "test@example.com";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(validResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/user/get-token-info"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(clientToken, clientEmail);

        // Assert
        assertTrue(result);
        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:8081/user/get-token-info"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class));
    }

    @Test
    void testVerifyTheToken_InvalidToken() {
        // Arrange
        String clientToken = "invalid-token";
        String clientEmail = "test@example.com";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(validResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(clientToken, clientEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyTheToken_InvalidEmail() {
        // Arrange
        String clientToken = "valid-server-token";
        String clientEmail = "wrong@example.com";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(validResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(clientToken, clientEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyTheToken_NullClientToken() {
        // Arrange
        String clientEmail = "test@example.com";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(validResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(null, clientEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyTheToken_NullClientEmail() {
        // Arrange
        String clientToken = "valid-server-token";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(validResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(clientToken, null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyTheToken_NullResponseBody() {
        // Arrange
        String clientToken = "valid-server-token";
        String clientEmail = "test@example.com";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(clientToken, clientEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyTheToken_ServerTokenNotString() {
        // Arrange
        String clientToken = "valid-server-token";
        String clientEmail = "test@example.com";

        Map<String, Object> invalidResponseBody = new HashMap<>();
        invalidResponseBody.put("token", 12345); // Token as integer instead of string

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", "test@example.com");
        invalidResponseBody.put("user", userMap);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(invalidResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(clientToken, clientEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyTheToken_UserNotMap() {
        // Arrange
        String clientToken = "valid-server-token";
        String clientEmail = "test@example.com";

        Map<String, Object> invalidResponseBody = new HashMap<>();
        invalidResponseBody.put("token", "valid-server-token");
        invalidResponseBody.put("user", "not-a-map"); // User as string instead of map

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(invalidResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(clientToken, clientEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyTheToken_EmailNotString() {
        // Arrange
        String clientToken = "valid-server-token";
        String clientEmail = "test@example.com";

        Map<String, Object> invalidResponseBody = new HashMap<>();
        invalidResponseBody.put("token", "valid-server-token");

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", 12345); // Email as integer instead of string
        invalidResponseBody.put("user", userMap);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(invalidResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = tokenService.VerifyTheToken(clientToken, clientEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyTheToken_RestClientException() {
        // Arrange
        String clientToken = "valid-server-token";
        String clientEmail = "test@example.com";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(Map.class))).thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(RestClientException.class, () -> {
            tokenService.VerifyTheToken(clientToken, clientEmail);
        });
    }
}
