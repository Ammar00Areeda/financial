package com.financial.controller;

import com.financial.dto.UserDto;
import com.financial.entity.User;
import com.financial.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * Get current authenticated user.
     *
     * @return the current user
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.debug("Fetching current user: {}", username);
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    /**
     * Get all users (Admin only).
     *
     * @return list of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Returns all users (Admin only)")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.debug("Fetching all users");
        
        List<UserDto> users = userService.findAll()
                .stream()
                .map(UserDto::fromEntity)
                .toList();
        
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID (Admin only).
     *
     * @param id the user ID
     * @return the user
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Returns a specific user (Admin only)")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.debug("Fetching user with id: {}", id);
        
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    /**
     * Update user (Admin only).
     *
     * @param id the user ID
     * @param updateRequest the update request
     * @return the updated user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Updates a user (Admin only)")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest updateRequest
    ) {
        log.info("Updating user with id: {}", id);
        
        User updateData = User.builder()
                .email(updateRequest.email())
                .firstName(updateRequest.firstName())
                .lastName(updateRequest.lastName())
                .password(updateRequest.password())
                .build();
        
        User updatedUser = userService.updateUser(id, updateData);
        
        return ResponseEntity.ok(UserDto.fromEntity(updatedUser));
    }

    /**
     * Delete user (Admin only).
     *
     * @param id the user ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * User update request DTO.
     */
    record UserUpdateRequest(
            String email,
            String firstName,
            String lastName,
            String password
    ) {}
}

