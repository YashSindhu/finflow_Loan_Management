package com.example.authservice.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.User;
import com.example.authservice.jwt.JwtUtil;
import com.example.authservice.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register - email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Admin-Secret", required = true) String adminSecret) {
        log.info("POST /auth/register/admin - email: {}", request.getEmail());
        return ResponseEntity.ok(authService.registerAdmin(request, adminSecret));
    }

    @PostMapping("/register/super-admin")
    public ResponseEntity<String> registerSuperAdmin(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Admin-Secret", required = true) String adminSecret) {
        log.info("POST /auth/register/super-admin - email: {}", request.getEmail());
        return ResponseEntity.ok(authService.registerSuperAdmin(request, adminSecret));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        log.info("POST /auth/login - email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authResponse.getToken())
                .header("Access-Control-Expose-Headers", HttpHeaders.AUTHORIZATION)
                .body(Map.of("role", authResponse.getRole(), "email", authResponse.getEmail()));
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("GET /auth/validate - missing or invalid Authorization header");
            return ResponseEntity.status(401).body("Missing token");
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        if (username != null && jwtUtil.validateToken(token, username)) {
            log.info("GET /auth/validate - token valid for user: {}", username);
            return ResponseEntity.ok(username);
        }
        log.warn("GET /auth/validate - invalid token");
        return ResponseEntity.status(401).body("Invalid token");
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        return ResponseEntity.ok(authService.getProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.updateProfile(email, body.get("name"), body.get("email")));
    }

    @PutMapping("/profile/password")
    public ResponseEntity<String> changePassword(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestBody Map<String, String> body) {
        authService.changePassword(email, body.get("oldPassword"), body.get("newPassword"));
        return ResponseEntity.ok("Password changed successfully");
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            log.warn("GET /auth/admin/users - access denied for role: {}", role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /auth/admin/users - by role: {}", role);
        return ResponseEntity.ok(authService.getAllUsers());
    }
    @PutMapping("/admin/users/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody Map<String, String> body) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            log.warn("PUT /auth/admin/users/{} - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("PUT /auth/admin/users/{} - new role: {}", id, body.get("role"));
        return ResponseEntity.ok(authService.updateUser(id, body.get("role")));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            log.warn("DELETE /auth/admin/users/{} - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("DELETE /auth/admin/users/{} - by role: {}", id, role);
        authService.deleteUser(id, role);
        return ResponseEntity.ok("User removed successfully");
    }
}
