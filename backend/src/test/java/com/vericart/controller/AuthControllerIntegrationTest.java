package com.vericart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vericart.dto.LoginRequest;
import com.vericart.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register — should register a new user")
    void shouldRegisterNewUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("integrationTest");
        req.setEmail("integration@test.com");
        req.setPassword("integration123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("integrationTest"));
    }

    @Test
    @DisplayName("POST /api/auth/register — should reject duplicate email")
    void shouldRejectDuplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("userA");
        req.setEmail("dup@test.com");
        req.setPassword("password123");

        // First registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Duplicate email
        RegisterRequest dup = new RegisterRequest();
        dup.setUsername("userB");
        dup.setEmail("dup@test.com");
        dup.setPassword("password456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/auth/login — should login with valid credentials")
    void shouldLogin() throws Exception {
        // Register first
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("loginTest");
        reg.setEmail("login@test.com");
        reg.setPassword("loginpass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        // Then login
        LoginRequest login = new LoginRequest();
        login.setEmail("login@test.com");
        login.setPassword("loginpass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login — should reject invalid password")
    void shouldRejectWrongPassword() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("login@test.com");
        login.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/register — should reject short username")
    void shouldRejectShortUsername() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("ab"); // too short (< 3)
        req.setEmail("short@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register — should reject invalid email")
    void shouldRejectInvalidEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("validUser");
        req.setEmail("not-an-email");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
