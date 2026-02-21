package br.com.oriontask.backend.users.dto;

import java.sql.Timestamp;
import java.util.UUID;

public record UserResponseDTO(
    UUID id, String name, String email, Timestamp createdAt, Timestamp updatedAt) {}
