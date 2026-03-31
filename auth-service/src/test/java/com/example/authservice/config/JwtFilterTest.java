package com.example.authservice.config;

import com.example.authservice.jwt.JwtUtil;
import com.example.authservice.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter();
        ReflectionTestUtils.setField(jwtFilter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(jwtFilter, "userDetailsService", userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_skipsJwtForAdminPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/admin/users");
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(any());
    }

    @Test
    void doFilterInternal_continuesWithNoAuthHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_continuesWithNonBearerHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_setsAuthenticationForValidToken() throws Exception {
        User userDetails = new User("user@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(request.getRequestURI()).thenReturn("/auth/validate");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(jwtUtil.extractUsername("validtoken")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("validtoken", "user@test.com")).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doesNotSetAuthForInvalidToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/validate");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtUtil.extractUsername("invalidtoken")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(
                new User("user@test.com", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        when(jwtUtil.validateToken("invalidtoken", "user@test.com")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
