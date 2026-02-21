package br.com.oriontask.backend.auth.dto;

import java.util.UUID;

public record AuthResponseDTO(String token, UUID id) {}
