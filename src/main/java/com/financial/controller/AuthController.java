package com.financial.controller;

import com.financial.dto.AuthenticationRequestDto;
import com.financial.dto.AuthenticationResponseDto;
import com.financial.dto.RegisterRequestDto;
import com.financial.dto.UserDto;
import com.financial.entity.User;
import com.financial.security.JwtUtil;
import com.financial.service.UserService;
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
 * Implements AuthApi interface which contains all OpenAPI documentation.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthApi {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    @PostMapping("/register")
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

    @Override
    @PostMapping("/login")
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

    @Override
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
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
