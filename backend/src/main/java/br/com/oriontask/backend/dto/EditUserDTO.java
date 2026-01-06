package br.com.oriontask.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record EditUserDTO(@NotBlank(message = "Name is required") String name,
                @NotBlank(message = "Username is required") String username) {
}
