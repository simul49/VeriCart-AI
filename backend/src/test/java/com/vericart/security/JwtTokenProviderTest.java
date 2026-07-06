package com.vericart.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // Use a 256-bit+ secret for HS256
        String secret = "this-is-a-test-secret-key-for-junit-testing-256bits!";
        jwtTokenProvider = new JwtTokenProvider(secret, 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Should generate a valid JWT token for a user")
    void shouldGenerateValidToken() {
        String token = jwtTokenProvider.generateToken(1L, "user@test.com", "CUSTOMER");

        assertNotNull(token);
        assertTrue(token.length() > 20);
        assertTrue(token.split("\\.").length == 3, "JWT should have 3 parts");
    }

    @Test
    @DisplayName("Should extract userId from token")
    void shouldExtractUserId() {
        String token = jwtTokenProvider.generateToken(42L, "test@example.com", "ADMIN");

        Long userId = jwtTokenProvider.getUserId(token);
        assertEquals(42L, userId);
    }

    @Test
    @DisplayName("Should extract email (subject) from token")
    void shouldExtractEmail() {
        String token = jwtTokenProvider.generateToken(1L, "alice@vericart.ai", "CUSTOMER");

        String email = jwtTokenProvider.getEmail(token);
        assertEquals("alice@vericart.ai", email);
    }

    @Test
    @DisplayName("Should extract role from token")
    void shouldExtractRole() {
        String token = jwtTokenProvider.generateToken(1L, "admin@vericart.ai", "ADMIN");

        String role = jwtTokenProvider.getRole(token);
        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("Should validate a legitimate token as true")
    void shouldValidateLegitimateToken() {
        String token = jwtTokenProvider.generateToken(1L, "user@test.com", "CUSTOMER");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should reject an expired token")
    void shouldRejectExpiredToken() {
        // Create a provider with 1ms expiration
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "another-test-secret-that-is-long-enough!!", 1L);
        String token = shortLived.generateToken(1L, "x@x.com", "CUSTOMER");

        // Wait for expiration
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        assertFalse(shortLived.validateToken(token));
    }

    @Test
    @DisplayName("Should reject a tampered token (invalid signature)")
    void shouldRejectTamperedToken() {
        String token = jwtTokenProvider.generateToken(1L, "user@test.com", "CUSTOMER");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertFalse(jwtTokenProvider.validateToken(tampered));
    }

    @Test
    @DisplayName("Should reject a completely invalid string")
    void shouldRejectInvalidString() {
        assertFalse(jwtTokenProvider.validateToken("not-a-jwt-token"));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("Should store custom claims correctly")
    void shouldStoreCustomClaims() {
        String token = jwtTokenProvider.generateToken(99L, "seller@shop.com", "SELLER");

        assertEquals(99L, jwtTokenProvider.getUserId(token));
        assertEquals("seller@shop.com", jwtTokenProvider.getEmail(token));
        assertEquals("SELLER", jwtTokenProvider.getRole(token));
    }
}
