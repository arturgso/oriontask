package br.com.oriontask.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileDTO(
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
        String name,
        
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, numbers, and underscore")
        String username,
        
        @Email(message = "Invalid email format")
        String email,
        
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
                message = "Password must contain uppercase, lowercase, number and special character")
        String newPassword) {
}
