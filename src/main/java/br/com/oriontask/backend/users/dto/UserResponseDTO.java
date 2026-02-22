package br.com.oriontask.backend.users.dto;

import java.sql.Timestamp;
import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String name,
    String email,
    Boolean isConfirmed,
    Timestamp createdAt,
    Timestamp updatedAt) {}
