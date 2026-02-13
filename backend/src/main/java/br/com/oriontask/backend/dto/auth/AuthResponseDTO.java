package br.com.oriontask.backend.dto.auth;

import java.util.UUID;

public record AuthResponseDTO(
        String token,
        UUID id,
        String username
) {}
