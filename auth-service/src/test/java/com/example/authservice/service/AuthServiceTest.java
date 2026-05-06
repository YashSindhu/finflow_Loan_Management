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

    // --- registerAdmin ---

    @Test
    void registerAdmin_savesAdminWithCorrectSecret() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Admin");
        req.setEmail("admin@test.com");
        req.setPassword("Admin@123");

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Admin@123")).thenReturn("encodedAdmin");

        String result = authService.registerAdmin(req, "finflow-admin-secret");

        assertEquals("Admin registered successfully", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerAdmin_throwsForInvalidSecret() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("admin@test.com");

        assertThrows(RuntimeException.class,
                () -> authService.registerAdmin(req, "wrong-secret"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerAdmin_throwsIfEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("admin@test.com");

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> authService.registerAdmin(req, "finflow-admin-secret"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerSuperAdmin_savesSuperAdminWithCorrectSecret() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Super Admin");
        req.setEmail("super@test.com");
        req.setPassword("Super@123");

        when(userRepository.findByEmail("super@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Super@123")).thenReturn("encodedSuper");

        String result = authService.registerSuperAdmin(req, "finflow-super-secret");

        assertEquals("Super admin registered successfully", result);
        verify(userRepository).save(argThat(saved -> "ROLE_SUPER_ADMIN".equals(saved.getRole())));
    }

    @Test
    void registerSuperAdmin_throwsForInvalidSecret() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("super@test.com");

        assertThrows(RuntimeException.class,
                () -> authService.registerSuperAdmin(req, "wrong-secret"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerSuperAdmin_throwsIfEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("super@test.com");

        when(userRepository.findByEmail("super@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> authService.registerSuperAdmin(req, "finflow-super-secret"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getProfile_returnsUserByEmail() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        User result = authService.getProfile("user@test.com");

        assertEquals("Yash Sindhu", result.getName());
    }

    @Test
    void getProfile_throwsIfUserNotFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.getProfile("missing@test.com"));
    }

    @Test
    void updateProfile_updatesNameAndEmail() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User result = authService.updateProfile("user@test.com", "New Name", "new@test.com");

        assertEquals("New Name", result.getName());
        assertEquals("new@test.com", result.getEmail());
    }

    @Test
    void updateProfile_keepsExistingValuesForBlankInput() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = authService.updateProfile("user@test.com", " ", "user@test.com");

        assertEquals("Yash Sindhu", result.getName());
        assertEquals("user@test.com", result.getEmail());
        verify(userRepository, never()).findByEmail("new@test.com");
    }

    @Test
    void updateProfile_throwsIfNewEmailAlreadyExists() {
        User otherUser = new User();
        otherUser.setEmail("taken@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(otherUser));

        assertThrows(RuntimeException.class,
                () -> authService.updateProfile("user@test.com", "New Name", "taken@test.com"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_updatesEncodedPassword() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        authService.changePassword("user@test.com", "oldPassword", "newPassword");

        assertEquals("newEncodedPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_throwsForWrongCurrentPassword() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authService.changePassword("user@test.com", "wrongPassword", "newPassword"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_deletesRegularUserForAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.deleteUser(1L, "ROLE_ADMIN");

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_blocksAdminDeletingAdmin() {
        user.setRole("ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.deleteUser(1L, "ROLE_ADMIN"));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_blocksSuperAdminDeletingSuperAdmin() {
        user.setRole("ROLE_SUPER_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.deleteUser(1L, "ROLE_SUPER_ADMIN"));
        verify(userRepository, never()).deleteById(anyLong());
    }
}
