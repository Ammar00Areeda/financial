package com.financial.controller;

import com.financial.dto.AuthenticationRequestDto;
import com.financial.dto.RegisterRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * API interface for Authentication operations.
 * Contains all OpenAPI documentation for authentication endpoints.
 */
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public interface AuthApi {

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or duplicate username/email"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> register(RegisterRequestDto registerRequest);

    @Operation(
            summary = "Authenticate user",
            description = "Login with username and password to receive JWT token. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> login(AuthenticationRequestDto authRequest);

    @Operation(
            summary = "Validate JWT token",
            description = "Check if JWT token is valid. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Token is invalid or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> validateToken(
            @Parameter(description = "JWT token to validate", required = true) String token);
}

