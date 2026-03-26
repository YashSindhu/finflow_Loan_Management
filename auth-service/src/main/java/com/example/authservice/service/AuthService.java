package com.example.authservice.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.User;
import com.example.authservice.jwt.JwtUtil;
import com.example.authservice.repository.UserRepository;

import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public String register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }
    	User user = new User();
    	user.setName(request.getName());
    	user.setEmail(request.getEmail());
    	user.setPassword(passwordEncoder.encode(request.getPassword()));
    	user.setRole("ROLE_USER");
        userRepository.save(user);
        log.info("User registered successfully: {}", request.getEmail());
        return "User registered successfully";
    }

    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(request.getEmail(), user.getRole());
        log.info("Login successful for email: {}, role: {}", user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getRole(), user.getEmail());
    }

    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    public User updateUser(Long id, String role) {
        log.info("Updating user id: {} to role: {}", id, role);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setRole(role);
        User updated = userRepository.save(user);
        log.info("User id: {} role updated to: {}", id, role);
        return updated;
    }
}
