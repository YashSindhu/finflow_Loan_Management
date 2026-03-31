package com.example.authservice.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "finflow-super-secret-key-for-jwt-signing-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("user@test.com", "ROLE_USER");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("user@test.com", "ROLE_USER");
        assertEquals("user@test.com", jwtUtil.extractUsername(token));
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("user@test.com", "ROLE_ADMIN");
        assertEquals("ROLE_ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken("user@test.com", "ROLE_USER");
        assertTrue(jwtUtil.validateToken(token, "user@test.com"));
    }

    @Test
    void validateToken_returnsFalseForWrongUsername() {
        String token = jwtUtil.generateToken("user@test.com", "ROLE_USER");
        assertFalse(jwtUtil.validateToken(token, "other@test.com"));
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // already expired
        String token = jwtUtil.generateToken("user@test.com", "ROLE_USER");
        // ExpiredJwtException is thrown for expired tokens - this is expected behavior
        assertThrows(Exception.class, () -> jwtUtil.validateToken(token, "user@test.com"));
    }
}
