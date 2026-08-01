package com.vaultix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaultix.dto.LoginRequest;
import com.vaultix.dto.LoginResponse;
import com.vaultix.dto.RegisterRequest;
import com.vaultix.dto.RegisterResponse;
import com.vaultix.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testRegisterEndpoint_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@vaultix.io");
        request.setPassword("SecureMasterPass123!");

        RegisterResponse response = new RegisterResponse(99L, "Registration successful");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(99L))
                .andExpect(jsonPath("$.email").value("newuser@vaultix.io"));
    }

    @Test
    void testLoginEndpoint_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@vaultix.io");
        request.setPassword("ValidPass123!");

        LoginResponse response = new LoginResponse("sample.jwt.access.token", "sample_refresh_token_string");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("sample.jwt.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("sample_refresh_token_string"));
    }
}
