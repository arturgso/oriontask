package br.com.oriontask.backend.utils;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Gets the currently authenticated user's ID from the security context.
     * The principal is set to UUID by JwtAuthenticationFilter.
     *
     * @return UUID of authenticated user
     * @throws IllegalStateException if user is not authenticated
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID) {
            return (UUID) principal;
        }

        throw new IllegalStateException("Invalid authentication principal type");
    }

    /**
     * Checks if there is a currently authenticated user.
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
               && authentication.getPrincipal() instanceof UUID;
    }
}

