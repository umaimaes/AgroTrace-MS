package com.example.usersmanage.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Token Blacklist Service Tests")
class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    @DisplayName("Blacklist Token - Should add token to blacklist")
    void testBlacklist() {
        String token = "testToken";
        assertFalse(tokenBlacklistService.isBlacklisted(token));

        tokenBlacklistService.blacklist(token);
        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    @DisplayName("Is Blacklisted - Should return false for unknown token")
    void testIsBlacklisted_False() {
        assertFalse(tokenBlacklistService.isBlacklisted("unknown"));
    }
}
