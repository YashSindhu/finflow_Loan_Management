package com.example.authservice.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.User;
import com.example.authservice.jwt.JwtUtil;
import com.example.authservice.service.AuthService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authResponse.getToken())
                .header("Access-Control-Expose-Headers", HttpHeaders.AUTHORIZATION)
                .body(Map.of("role", authResponse.getRole(), "email", authResponse.getEmail()));
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing token");
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        if (username != null && jwtUtil.validateToken(token, username)) {
            return ResponseEntity.ok(username);
        }
        return ResponseEntity.status(401).body("Invalid token");
    }

    // Admin: list all users
    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(authService.getAllUsers());
    }

    // Admin: update user role
    @PutMapping("/admin/users/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody Map<String, String> body) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(authService.updateUser(id, body.get("role")));
    }
}
