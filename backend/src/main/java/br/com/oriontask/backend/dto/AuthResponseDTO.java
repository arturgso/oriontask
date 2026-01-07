package br.com.oriontask.backend.dto;

import java.util.UUID;

public record AuthResponseDTO(
        String token,
        UUID id,
        String username,
        String name
) {}
