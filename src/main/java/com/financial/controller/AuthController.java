package com.financial.controller;

import com.financial.dto.AuthenticationRequestDto;
import com.financial.dto.AuthenticationResponseDto;
import com.financial.dto.RegisterRequestDto;
import com.financial.dto.UserDto;
import com.financial.entity.User;
import com.financial.security.JwtUtil;
import com.financial.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user.
     *
     * @param registerRequest the registration request
     * @return the created user details
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user", 
            description = "Creates a new user account. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or duplicate username/email"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        log.info("Registration request for username: {}", registerRequest.getUsername());

        try {
            // Check if username already exists
            if (userService.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Username is already taken"));
            }

            // Check if email already exists
            if (userService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Email is already in use"));
            }

            // Create new user
            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(registerRequest.getPassword())
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .role(User.Role.USER)
                    .isEnabled(true)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .build();

            User createdUser = userService.createUser(user);
            log.info("User registered successfully: {}", createdUser.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserDto.fromEntity(createdUser));

        } catch (Exception e) {
            log.error("Error during registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Authenticate user and return JWT token.
     *
     * @param authRequest the authentication request
     * @return the authentication response with JWT token
     */
    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user", 
            description = "Login with username and password to receive JWT token. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequestDto authRequest) {
        log.info("Login attempt for username: {}", authRequest.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            // Load user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate JWT token
            String jwt = jwtUtil.generateToken(userDetails);

            // Update last login time
            userService.updateLastLogin(user.getUsername());

            log.info("User logged in successfully: {}", user.getUsername());

            return ResponseEntity.ok(AuthenticationResponseDto.of(jwt, user));

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for username: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid username or password"));
        } catch (org.springframework.security.authentication.DisabledException e) {
            log.error("Account is disabled for username: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Account is disabled"));
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Login failed: " + e.getMessage()));
        }
    }

    /**
     * Validate JWT token.
     *
     * @param token the JWT token
     * @return validation response
     */
    @GetMapping("/validate")
    @Operation(
            summary = "Validate JWT token", 
            description = "Check if JWT token is valid. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Token is invalid or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> validateToken(
            @Parameter(description = "JWT token to validate", required = true) @RequestParam String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            boolean isValid = jwtUtil.validateToken(token, userDetails);

            if (isValid) {
                return ResponseEntity.ok(new ValidationResponse(true, "Token is valid"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ValidationResponse(false, "Token is invalid"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidationResponse(false, "Token validation failed"));
        }
    }

    /**
     * Error response DTO.
     */
    record ErrorResponse(String message) {}

    /**
     * Validation response DTO.
     */
    record ValidationResponse(boolean valid, String message) {}
}

