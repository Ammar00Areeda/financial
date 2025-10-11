package com.financial.service;

import com.financial.entity.User;
import com.financial.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing user operations and Spring Security integration.
 * 
 * <p>This service handles user account management including registration, profile updates,
 * authentication, and password management. It implements {@link UserDetailsService} to
 * integrate with Spring Security for authentication.</p>
 * 
 * <p><b>Security:</b> Most read operations require authentication, while user creation
 * is typically public for registration. Password fields are automatically encoded using
 * BCrypt before storage.</p>
 * 
 * @see User
 * @see UserRepository
 * @see UserDetailsService
 */
@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a UserService with lazy password encoder to break circular dependency.
     * 
     * <p>The password encoder is lazily loaded to prevent circular dependency issues
     * during Spring bean initialization, as SecurityConfig depends on UserService.</p>
     *
     * @param userRepository the user repository for database operations
     * @param passwordEncoder the password encoder (lazy loaded to break circular dependency)
     */
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Loads user by username for Spring Security authentication.
     * 
     * <p>This method is called by Spring Security during the authentication process.
     * It retrieves the user from the database and returns it as a UserDetails object.</p>
     * 
     * <p><b>Security:</b> This method is called internally by Spring Security framework.
     * The User entity implements UserDetails interface.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Typically called by Spring Security, not directly
     * // But can be used for manual authentication:
     * UserDetails userDetails = userService.loadUserByUsername("john_doe");
     * UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
     *     userDetails, null, userDetails.getAuthorities()
     * );
     * SecurityContextHolder.getContext().setAuthentication(auth);
     * }</pre>
     *
     * @param username the username to search for
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if no user is found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Find user by username.
     *
     * @param username the username
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email.
     *
     * @param email the email
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by ID.
     *
     * @param id the user ID
     * @return Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Get all users.
     *
     * @return list of all users
     */
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    /**
     * Creates a new user account.
     * 
     * <p>Validates that the username and email are unique, encodes the password using
     * BCrypt, and sets the default role to USER if not specified. This method is typically
     * used for user registration.</p>
     * 
     * <p><b>Security:</b> The password is automatically encoded using BCrypt before storage.
     * Passwords are never stored in plain text. This method is typically accessible without
     * authentication for registration purposes.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Registration example
     * User newUser = User.builder()
     *     .username("john_doe")
     *     .email("john@example.com")
     *     .password("SecurePassword123!")
     *     .firstName("John")
     *     .lastName("Doe")
     *     .build();
     * 
     * try {
     *     User created = userService.createUser(newUser);
     *     System.out.println("User registered with ID: " + created.getId());
     *     System.out.println("Role assigned: " + created.getRole());
     *     // Note: password is now encoded and cannot be retrieved
     * } catch (IllegalArgumentException e) {
     *     System.err.println("Registration failed: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param user the user to create (must include username, email, and password)
     * @return the persisted user with generated ID, encoded password, and default role
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public User createUser(User user) {
        log.info("Creating new user: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        
        return userRepository.save(user);
    }

    /**
     * Updates an existing user account.
     * 
     * <p>Allows updating of email, first name, last name, and password. Email must remain
     * unique across all users. If a new password is provided, it is automatically encoded
     * before storage. The username cannot be changed.</p>
     * 
     * <p><b>Security:</b> If a password is provided, it is automatically encoded using BCrypt.
     * This method should be protected to ensure users can only update their own profile
     * (except for administrators).</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Update user profile
     * User updates = new User();
     * updates.setEmail("newemail@example.com");
     * updates.setFirstName("Jane");
     * updates.setPassword("NewPassword123!"); // Will be encoded
     * 
     * try {
     *     User updated = userService.updateUser(currentUserId, updates);
     *     System.out.println("Profile updated successfully");
     *     System.out.println("New email: " + updated.getEmail());
     * } catch (IllegalArgumentException e) {
     *     System.err.println("Update failed: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param id the ID of the user to update
     * @param updatedUser object containing the fields to update (null fields are ignored)
     * @return the updated user with changes applied
     * @throws IllegalArgumentException if the user doesn't exist or if the new email
     *         already exists for another user
     */
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        log.info("Updating user with id: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Update fields
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + updatedUser.getEmail());
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }

    /**
     * Updates the last login timestamp for a user.
     * 
     * <p>This method is typically called after successful authentication to track when
     * users last accessed the system. It updates the lastLogin field to the current
     * date and time.</p>
     * 
     * <p><b>Security:</b> This method should be called by the authentication system
     * after successful login. It does not require additional authorization as it only
     * updates a timestamp.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Called after successful authentication
     * @Override
     * protected void successfulAuthentication(HttpServletRequest request,
     *                                       HttpServletResponse response,
     *                                       FilterChain chain,
     *                                       Authentication authResult) {
     *     String username = authResult.getName();
     *     userService.updateLastLogin(username);
     *     logger.info("User {} logged in successfully", username);
     * }
     * }</pre>
     *
     * @param username the username of the user who logged in
     */
    @Transactional
    public void updateLastLogin(String username) {
        log.debug("Updating last login for user: {}", username);
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    /**
     * Deletes a user account.
     * 
     * <p>This operation is permanent and cannot be undone. All user data including
     * accounts, transactions, loans, and recurring expenses will be cascade deleted
     * based on database constraints. Use with caution.</p>
     * 
     * <p><b>Security:</b> This method should be highly restricted, typically only
     * available to administrators or for account self-deletion with proper confirmation.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Delete user account (typically with confirmation)
     * try {
     *     userService.deleteUser(userId);
     *     System.out.println("Account deleted successfully");
     *     // User should be logged out and redirected
     * } catch (IllegalArgumentException e) {
     *     System.err.println("Deletion failed: " + e.getMessage());
     * }
     * 
     * // In a controller with confirmation:
     * @DeleteMapping("/users/{id}")
     * @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
     * public ResponseEntity<Void> deleteAccount(@PathVariable Long id,
     *                                           @RequestParam String confirmation) {
     *     if (!"DELETE".equals(confirmation)) {
     *         return ResponseEntity.badRequest().build();
     *     }
     *     userService.deleteUser(id);
     *     return ResponseEntity.noContent().build();
     * }
     * }</pre>
     *
     * @param id the ID of the user to delete
     * @throws IllegalArgumentException if the user doesn't exist
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Check if username exists.
     *
     * @param username the username
     * @return true if exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists.
     *
     * @param email the email
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}

