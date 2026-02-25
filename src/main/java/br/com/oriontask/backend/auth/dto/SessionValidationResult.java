package br.com.oriontask.backend.auth.dto;

import java.util.UUID;

public record SessionValidationResult(
    SessionStatus status, String accessToken, String refreshToken, UUID userId) {
  public enum SessionStatus {
    VALID, // Access Token original era válido, Refresh Token também (ou não precisou)
    REFRESHED, // Access Token inválido, mas Refresh Token válido e gerou novos tokens
    UNAUTHORIZED // Nem Access Token nem Refresh Token (ou Refresh Token inválido)
  }
}
