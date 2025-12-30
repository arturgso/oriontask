package br.com.oriontask.backend.dto;

import java.sql.Timestamp;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String username,
        Timestamp createdAt,
        Timestamp updatedAt) {

}
