package com.productfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.productfinder.dto.LoginRequest;
import com.productfinder.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_withValidData_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setEmail("testuser@example.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void register_withDuplicateUsername_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("dupuser");
        req.setEmail("first@example.com");
        req.setPassword("password123");

        // Register once
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Register again with same username
        req.setEmail("second@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Username already taken"));
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        // Register first
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("loginuser");
        reg.setEmail("loginuser@example.com");
        reg.setPassword("mypassword");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Then login
        LoginRequest login = new LoginRequest();
        login.setUsername("loginuser");
        login.setPassword("mypassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("bearer"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("wrongpassuser");
        reg.setEmail("wrongpass@example.com");
        reg.setPassword("correctpassword");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername("wrongpassuser");
        login.setPassword("wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withShortPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("shortpwuser");
        req.setEmail("shortpw@example.com");
        req.setPassword("abc"); // too short

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
