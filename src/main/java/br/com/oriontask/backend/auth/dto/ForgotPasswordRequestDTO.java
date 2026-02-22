package br.com.oriontask.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format")
        String email) {}
