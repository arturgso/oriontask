package br.com.oriontask.backend.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDTO(
    String name, @Size(min = 6, max = 12) String username, @Email String email) {}
