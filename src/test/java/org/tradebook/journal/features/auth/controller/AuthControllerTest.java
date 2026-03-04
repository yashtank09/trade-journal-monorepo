package org.tradebook.journal.features.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tradebook.journal.features.auth.dto.response.AuthResponse;
import org.tradebook.journal.features.auth.dto.request.LoginRequest;
import org.tradebook.journal.features.auth.dto.request.RegisterRequest;
import org.tradebook.journal.features.auth.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for this test
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private org.tradebook.journal.config.security.JwtService jwtService;

    @MockBean
    private org.tradebook.journal.config.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnOk_WhenRequestIsValid() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                            .email("john@example.com")
                            .password("password")
                            .currency("USD")
                            .build();

            AuthResponse response = AuthResponse.builder()
                            .token("jwt-token")
                            .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void authenticate_ShouldReturnOk_WhenCredentialsAreValid() throws Exception {
            LoginRequest request = LoginRequest.builder()
                            .email("john@example.com")
                            .password("password")
                            .build();

            AuthResponse response = AuthResponse.builder()
                            .token("jwt-token")
                            .build();

            when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}
