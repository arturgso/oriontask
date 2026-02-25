package br.com.oriontask.backend.refreshtoken.service;

import br.com.oriontask.backend.auth.service.TokenService;
import br.com.oriontask.backend.refreshtoken.models.RefreshToken;
import br.com.oriontask.backend.refreshtoken.repository.RefreshTokenRepository;
import br.com.oriontask.backend.refreshtoken.utils.HashUtils;
import br.com.oriontask.backend.users.model.Users;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

  private final TokenService tokenService;
  private final RefreshTokenRepository refreshTokenRepository;

  public String createRefreshToken(Users user) {
    String rawRefreshToken = tokenService.generateRefreshToken(user);
    DecodedJWT decodedJWT = tokenService.validateRefreshToken(rawRefreshToken);

    return refreshTokenRepository
        .findByUserId(user.getId())
        .map(
            existingToken -> {
              existingToken.setTokenHash(HashUtils.sha256(rawRefreshToken));
              existingToken.setExpirationAt(new Timestamp(decodedJWT.getExpiresAt().getTime()));
              refreshTokenRepository.save(existingToken);
              return rawRefreshToken;
            })
        .orElseGet(
            () -> {
              RefreshToken newRefreshToken =
                  RefreshToken.builder()
                      .user(user)
                      .tokenHash(HashUtils.sha256(rawRefreshToken))
                      .expirationAt(new Timestamp(decodedJWT.getExpiresAt().getTime()))
                      .build();
              refreshTokenRepository.save(newRefreshToken);
              return rawRefreshToken;
            });
  }

  public DecodedJWT validateRefreshToken(String rawRefreshToken) {
    try {
      DecodedJWT decodedJWT = tokenService.validateRefreshToken(rawRefreshToken);
      UUID userId = UUID.fromString(decodedJWT.getSubject());
      String refreshHash = HashUtils.sha256(rawRefreshToken);

      RefreshToken dbToken =
          refreshTokenRepository
              .findByUserId(userId)
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Refresh token not found or invalid for user: " + userId));

      if (!dbToken.getTokenHash().equals(refreshHash)) {
        log.warn("Refresh token hash mismatch for user: {}. Deleting token from DB.", userId);
        refreshTokenRepository.delete(dbToken);
        throw new IllegalArgumentException("Invalid refresh token (hash mismatch)");
      }

      if (dbToken.getExpirationAt().toInstant().isBefore(Instant.now())) {
        log.warn("Refresh token for user {} expired in DB. Deleting token from DB.", userId);
        refreshTokenRepository.delete(dbToken);
        throw new IllegalArgumentException("Refresh token expired");
      }

      return decodedJWT;

    } catch (IllegalArgumentException e) {
      log.warn("Validation failed: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during refresh token validation: {}", e.getMessage(), e);
      throw new IllegalArgumentException("Invalid refresh token (general error)");
    }
  }
}
