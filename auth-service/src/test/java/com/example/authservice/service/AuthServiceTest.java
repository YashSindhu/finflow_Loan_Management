package com.example.authservice.service;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.User;
import com.example.authservice.jwt.JwtUtil;
import com.example.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Yash Sindhu");
        user.setEmail("user@test.com");
        user.setPassword("encodedPassword");
        user.setRole("ROLE_USER");
    }

    // --- register ---

    @Test
    void register_savesUserAndReturnsSuccessMessage() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Yash Sindhu");
        req.setEmail("user@test.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        String result = authService.register(req);

        assertEquals("User registered successfully", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsIfEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@test.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    // --- login ---

    @Test
    void login_returnsTokenOnSuccess() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@test.com");
        req.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("user@test.com", "ROLE_USER")).thenReturn("mock.jwt.token");

        AuthResponse result = authService.login(req);

        assertEquals("mock.jwt.token", result.getToken());
        assertEquals("ROLE_USER", result.getRole());
        assertEquals("user@test.com", result.getEmail());
    }

    @Test
    void login_throwsIfUserNotFound() {
        AuthRequest req = new AuthRequest();
        req.setEmail("unknown@test.com");
        req.setPassword("password123");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(req));
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = authService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("user@test.com", result.get(0).getEmail());
    }

    // --- updateUser ---

    @Test
    void updateUser_updatesRoleSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        User result = authService.updateUser(1L, "ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", result.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_throwsIfUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.updateUser(99L, "ROLE_ADMIN"));
    }
}
