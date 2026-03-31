package com.example.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void authRequest_gettersSetters() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@test.com");
        req.setPassword("Password@123");
        assertEquals("user@test.com", req.getEmail());
        assertEquals("Password@123", req.getPassword());
    }

    @Test
    void authResponse_gettersSetters() {
        AuthResponse res = new AuthResponse("token", "ROLE_USER", "user@test.com");
        assertEquals("token", res.getToken());
        assertEquals("ROLE_USER", res.getRole());
        assertEquals("user@test.com", res.getEmail());

        res.setToken("newtoken");
        res.setRole("ROLE_ADMIN");
        res.setEmail("admin@test.com");
        assertEquals("newtoken", res.getToken());
        assertEquals("ROLE_ADMIN", res.getRole());
        assertEquals("admin@test.com", res.getEmail());
    }

    @Test
    void registerRequest_gettersSetters() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Yash Sindhu");
        req.setEmail("user@test.com");
        req.setPassword("Password@123");
        assertEquals("Yash Sindhu", req.getName());
        assertEquals("user@test.com", req.getEmail());
        assertEquals("Password@123", req.getPassword());
    }
}
