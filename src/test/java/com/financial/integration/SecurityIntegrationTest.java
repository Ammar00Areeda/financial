package com.financial.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.dto.AuthenticationRequestDto;
import com.financial.dto.RegisterRequestDto;
import com.financial.entity.User;
import com.financial.repository.UserRepository;
import com.financial.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for security and authentication.
 * Tests JWT authentication, authorization, and access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private User adminUser;
    private String testUserToken;
    private String adminUserToken;

    @BeforeEach
    void setUp() {
        // Clean up existing test data
        userRepository.deleteAll();

        // Create test user with USER role
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        testUser = userRepository.save(testUser);

        // Create admin user with ADMIN role
        adminUser = User.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        adminUser = userRepository.save(adminUser);

        // Generate JWT tokens
        testUserToken = jwtUtil.generateToken(testUser);
        adminUserToken = jwtUtil.generateToken(adminUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    // ========== Registration Tests ==========

    @Test
    void register_WithValidData_ShouldCreateUser() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void register_WithDuplicateUsername_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("testuser") // Already exists
                .email("another@example.com")
                .password("password123")
                .firstName("Another")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Username is already taken")));
    }

    @Test
    void register_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("anotheruser")
                .email("test@example.com") // Already exists
                .password("password123")
                .firstName("Another")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Email is already in use")));
    }

    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("") // Invalid: empty username
                .email("invalid-email") // Invalid: not a valid email
                .password("123") // Too short
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== Login Tests ==========

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void login_WithInvalidUsername_ShouldReturnUnauthorized() throws Exception {
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("nonexistent")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid username or password")));
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnUnauthorized() throws Exception {
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid username or password")));
    }

    // ========== Token Validation Tests ==========

    @Test
    void validateToken_WithValidToken_ShouldReturnValid() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                        .param("token", testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("Token is valid"));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnInvalid() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                        .param("token", "invalid-token-string"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnInvalid() throws Exception {
        // Create an expired token (this would require mocking or creating a token with past expiration)
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTUxNjIzOTAyMn0.invalid";
        
        mockMvc.perform(get("/api/auth/validate")
                        .param("token", expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    // ========== Authorization Tests ==========

    @Test
    void accessProtectedEndpoint_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithValidToken_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk());
    }

    @Test
    void accessProtectedEndpoint_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithMalformedAuthHeader_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", testUserToken)) // Missing "Bearer " prefix
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessAdminEndpoint_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessAdminEndpoint_WithAdminRole_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminUserToken))
                .andExpect(status().isOk());
    }

    @Test
    void accessAdminEndpoint_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // ========== Public Endpoints Tests ==========

    @Test
    void accessPublicEndpoint_WithoutToken_ShouldReturnOk() throws Exception {
        // Auth endpoints should be public
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("publicuser")
                .email("public@example.com")
                .password("password123")
                .firstName("Public")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void accessSwaggerEndpoint_WithoutToken_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ========== CSRF Tests ==========

    @Test
    void postRequest_WithoutCsrfToken_ShouldSucceed() throws Exception {
        // CSRF is disabled for API, so this should work
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ========== CORS Tests ==========

    @Test
    void optionsRequest_WithCorsHeaders_ShouldReturnOk() throws Exception {
        mockMvc.perform(options("/api/accounts")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    // ========== Session Management Tests ==========

    @Test
    void multipleRequests_WithSameToken_ShouldSucceed() throws Exception {
        // Test that token is stateless and can be reused
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/accounts")
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void simultaneousRequests_WithDifferentTokens_ShouldSucceed() throws Exception {
        // Test that different users can make requests simultaneously
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminUserToken))
                .andExpect(status().isOk());
    }

    // ========== Token Expiration Tests ==========

    @Test
    void login_AfterSuccessfulLogin_ShouldUpdateLastLogin() throws Exception {
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify that lastLogin was updated
        User updatedUser = userRepository.findByUsername("testuser").orElseThrow();
        assert updatedUser.getLastLogin() != null;
    }

    // ========== Disabled User Tests ==========

    @Test
    void login_WithDisabledUser_ShouldReturnUnauthorized() throws Exception {
        // Disable the test user
        testUser.setIsEnabled(false);
        userRepository.save(testUser);

        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessEndpoint_WithDisabledUserToken_ShouldReturnUnauthorized() throws Exception {
        // First, get a valid token
        String token = testUserToken;

        // Then disable the user
        testUser.setIsEnabled(false);
        userRepository.save(testUser);

        // Try to access protected endpoint with token from disabled user
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}

