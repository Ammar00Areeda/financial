package com.financial.controller;

import com.financial.dto.UserResponseDto;
import com.financial.dto.UserUpdateRequestDto;
import com.financial.entity.User;
import com.financial.service.UserService;
import jakarta.validation.Valid;
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
 * Implements UserApi interface which contains all OpenAPI documentation.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.debug("Fetching current user: {}", username);
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.debug("Fetching all users");
        
        List<UserResponseDto> users = userService.findAll()
                .stream()
                .map(UserResponseDto::fromEntity)
                .toList();
        
        return ResponseEntity.ok(users);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.debug("Fetching user with id: {}", id);
        
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto updateRequest) {
        log.info("Updating user with id: {}", id);
        
        User updateData = User.builder()
                .email(updateRequest.getEmail())
                .firstName(updateRequest.getFirstName())
                .lastName(updateRequest.getLastName())
                .password(updateRequest.getPassword())
                .build();
        
        User updatedUser = userService.updateUser(id, updateData);
        
        return ResponseEntity.ok(UserResponseDto.fromEntity(updatedUser));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
