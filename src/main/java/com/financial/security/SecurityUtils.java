package com.financial.security;

import com.financial.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility class for security-related operations.
 */
@Slf4j
public class SecurityUtils {

    /**
     * Get the currently authenticated user.
     *
     * @return the authenticated User object
     * @throws IllegalStateException if no user is authenticated
     */
    public static User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found in security context");
            throw new IllegalStateException("User is not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        } else if (principal instanceof UserDetails) {
            log.error("Principal is UserDetails but not User entity");
            throw new IllegalStateException("Unable to extract User from authentication");
        }
        
        log.error("Principal is not a valid User object: {}", principal.getClass());
        throw new IllegalStateException("Invalid authentication principal");
    }

    /**
     * Get the currently authenticated user ID.
     *
     * @return the authenticated user's ID
     * @throws IllegalStateException if no user is authenticated
     */
    public static Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }

    /**
     * Get the currently authenticated username.
     *
     * @return the authenticated username
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getAuthenticatedUsername() {
        return getAuthenticatedUser().getUsername();
    }

    /**
     * Check if there is an authenticated user.
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null 
                && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof User;
    }
}

