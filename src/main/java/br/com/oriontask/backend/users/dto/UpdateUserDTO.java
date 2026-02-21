package br.com.oriontask.backend.users.dto;

import jakarta.validation.constraints.Email;

public record UpdateUserDTO(String name, @Email String email) {}
