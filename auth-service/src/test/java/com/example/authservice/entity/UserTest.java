package com.example.authservice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void user_gettersSetters() {
        User user = new User();
        user.setId(1L);
        user.setName("Yash Sindhu");
        user.setEmail("user@test.com");
        user.setPassword("encodedPassword");
        user.setRole("ROLE_USER");

        assertEquals(1L, user.getId());
        assertEquals("Yash Sindhu", user.getName());
        assertEquals("user@test.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("ROLE_USER", user.getRole());
    }

    @Test
    void user_builder() {
        User user = User.builder()
                .id(1L)
                .name("Yash Sindhu")
                .email("user@test.com")
                .password("encodedPassword")
                .role("ROLE_ADMIN")
                .build();

        assertEquals(1L, user.getId());
        assertEquals("ROLE_ADMIN", user.getRole());
    }
}
