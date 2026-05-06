package com.example.authservice.service;


import java.util.List;

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

    public String registerAdmin(RegisterRequest request, String adminSecret) {
        log.info("Admin registration attempt for email: {}", request.getEmail());
        if (!"finflow-admin-secret".equals(adminSecret)) {
            log.warn("Admin registration failed - invalid secret for email: {}", request.getEmail());
            throw new RuntimeException("Invalid admin secret");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Admin registration failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_ADMIN");
        userRepository.save(user);
        log.info("Admin registered successfully: {}", request.getEmail());
        return "Admin registered successfully";
    }

    public String registerSuperAdmin(RegisterRequest request, String adminSecret) {
        log.info("Super admin registration attempt for email: {}", request.getEmail());
        if (!"finflow-super-secret".equals(adminSecret)) {
            log.warn("Super admin registration failed - invalid secret");
            throw new RuntimeException("Invalid super admin secret");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_SUPER_ADMIN");
        userRepository.save(user);
        log.info("Super admin registered successfully: {}", request.getEmail());
        return "Super admin registered successfully";
    }

    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(String email, String name, String newEmail) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (name != null && !name.isBlank()) user.setName(name);
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(email)) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(newEmail);
        }
        return userRepository.save(user);
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(Long id, String requestorRole) {
        log.info("Delete user id: {} requested by role: {}", id, requestorRole);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // ROLE_ADMIN can only delete ROLE_USER
        if ("ROLE_ADMIN".equals(requestorRole) && !"ROLE_USER".equals(user.getRole())) {
            log.warn("Delete denied - admin cannot delete role: {}", user.getRole());
            throw new RuntimeException("Admins can only remove regular users");
        }
        // ROLE_SUPER_ADMIN can delete ROLE_USER and ROLE_ADMIN but not other super admins
        if ("ROLE_SUPER_ADMIN".equals(requestorRole) && "ROLE_SUPER_ADMIN".equals(user.getRole())) {
            log.warn("Delete denied - cannot delete another super admin");
            throw new RuntimeException("Cannot remove another super admin");
        }

        userRepository.deleteById(id);
        log.info("User id: {} deleted successfully", id);
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
