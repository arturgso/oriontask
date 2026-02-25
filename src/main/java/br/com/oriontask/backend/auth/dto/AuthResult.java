package br.com.oriontask.backend.auth.dto;

import java.util.UUID;

public record AuthResult(String accessToken, String refreshToken, UUID userId) {}
