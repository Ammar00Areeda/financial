package com.financial.controller;

import com.financial.dto.UserResponseDto;
import com.financial.dto.UserUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * API interface for User management operations.
 * Contains all OpenAPI documentation for user endpoints.
 */
@Tag(name = "Users", description = "User management endpoints (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public interface UserApi {

    @Operation(
            summary = "Get current user",
            description = "Returns the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<UserResponseDto> getCurrentUser();

    @Operation(
            summary = "Get all users",
            description = "Returns all users (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all users"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin only"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<UserResponseDto>> getAllUsers();

    @Operation(
            summary = "Get user by ID",
            description = "Returns a specific user (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin only"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<UserResponseDto> getUserById(Long id);

    @Operation(
            summary = "Update user",
            description = "Updates a user (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin only"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<UserResponseDto> updateUser(Long id, UserUpdateRequestDto updateRequest);

    @Operation(
            summary = "Delete user",
            description = "Deletes a user (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin only"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Void> deleteUser(Long id);
}

