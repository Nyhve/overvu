package com.overvu.backend.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import com.overvu.backend.security.JwtService;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void register_returnsCreated_whenRequestIsValid() throws Exception {
        UserResponse response = new UserResponse(1L, "test@example.com", LocalDateTime.now());
        when(userService.register(any(RegisterUserRequest.class))).thenReturn(response);

        RegisterUserRequest request = new RegisterUserRequest("test@example.com", "sicheresPasswort123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void register_returnsBadRequest_whenPasswordTooShort() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("test@example.com", "kurz");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returnsBadRequest_whenEmailIsInvalid() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("keine-email", "sicheresPasswort123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returnsConflict_whenEmailAlreadyExists() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("test@example.com"));

        RegisterUserRequest request = new RegisterUserRequest("test@example.com", "sicheresPasswort123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered: test@example.com"));
    }

    @Test
    void login_returnsOkAndToken_whenCredentialsAreValid() throws Exception {
        LoginResponse response = new LoginResponse("fake-jwt-token");
        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest("test@example.com", "sicheresPasswort123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void login_returnsUnauthorized_whenCredentialsAreInvalid() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        LoginRequest request = new LoginRequest("test@example.com", "falschesPasswort");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_returnsBadRequest_whenEmailIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("", "sicheresPasswort123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}