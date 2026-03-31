package com.example.authservice.controller;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.User;
import com.example.authservice.jwt.JwtUtil;
import com.example.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "jwtUtil", jwtUtil);
    }

    // --- register ---

    @Test
    void register_returnsOk() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@test.com");
        when(authService.register(req)).thenReturn("User registered successfully");
        ResponseEntity<String> response = controller.register(req);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("User registered successfully", response.getBody());
    }

    // --- login ---

    @Test
    void login_returnsOkWithRoleAndEmail() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@test.com");
        req.setPassword("Password@123");
        AuthResponse authResponse = new AuthResponse("token123", "ROLE_USER", "user@test.com");
        when(authService.login(req)).thenReturn(authResponse);

        ResponseEntity<?> response = controller.login(req);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    // --- validate ---

    @Test
    void validate_returnsOkForValidToken() {
        when(jwtUtil.extractUsername("validtoken")).thenReturn("user@test.com");
        when(jwtUtil.validateToken("validtoken", "user@test.com")).thenReturn(true);

        ResponseEntity<String> response = controller.validate("Bearer validtoken");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("user@test.com", response.getBody());
    }

    @Test
    void validate_returns401ForMissingHeader() {
        ResponseEntity<String> response = controller.validate(null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void validate_returns401ForInvalidToken() {
        when(jwtUtil.extractUsername("badtoken")).thenReturn("user@test.com");
        when(jwtUtil.validateToken("badtoken", "user@test.com")).thenReturn(false);

        ResponseEntity<String> response = controller.validate("Bearer badtoken");
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void validate_returns401ForNonBearerHeader() {
        ResponseEntity<String> response = controller.validate("Basic abc123");
        assertEquals(401, response.getStatusCode().value());
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_returnsOkForAdmin() {
        User user = new User();
        user.setEmail("user@test.com");
        when(authService.getAllUsers()).thenReturn(List.of(user));

        ResponseEntity<List<User>> response = controller.getAllUsers("ROLE_ADMIN");
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getAllUsers_returns403ForNonAdmin() {
        ResponseEntity<List<User>> response = controller.getAllUsers("ROLE_USER");
        assertEquals(403, response.getStatusCode().value());
        verify(authService, never()).getAllUsers();
    }

    // --- updateUser ---

    @Test
    void updateUser_returnsOkForAdmin() {
        User user = new User();
        user.setRole("ROLE_ADMIN");
        when(authService.updateUser(1L, "ROLE_ADMIN")).thenReturn(user);

        ResponseEntity<User> response = controller.updateUser(1L, "ROLE_ADMIN", Map.of("role", "ROLE_ADMIN"));
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateUser_returns403ForNonAdmin() {
        ResponseEntity<User> response = controller.updateUser(1L, "ROLE_USER", Map.of("role", "ROLE_ADMIN"));
        assertEquals(403, response.getStatusCode().value());
    }
}
