package br.com.oriontask.backend.auth.policy;

import br.com.oriontask.backend.auth.dto.SessionValidationResult;
import br.com.oriontask.backend.auth.dto.SessionValidationResult.SessionStatus;
import br.com.oriontask.backend.auth.service.TokenService;
import br.com.oriontask.backend.refreshtoken.service.RefreshTokenService;
import br.com.oriontask.backend.shared.utils.UserLookupService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthPolicy {

  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final RefreshTokenService refreshTokenService;
  private final UserLookupService userLookupService;

  public void verifyPasswordHash(String reqPassword, String userPassword) {
    if (!passwordEncoder.matches(reqPassword, userPassword)) {
      log.warn("Login failed: invalid password");
      throw new IllegalArgumentException("Invalid credentials");
    }
  }

  public void isEmailConfirmed(boolean isConfirmed) {
    if (!isConfirmed) {
      log.warn("Login blocked: email not confirmed");
      throw new IllegalArgumentException("Please confirm your email before logging in.");
    }
  }

  public SessionValidationResult applySessionValidationAndRefresh(
      String accessToken, String refreshToken) {
    boolean accessTokenOriginallyValid = false;
    DecodedJWT originalDecodedAccessToken = null;

    try {
      if (accessToken != null) {
        originalDecodedAccessToken = tokenService.validateAccessToken(accessToken);
        accessTokenOriginallyValid = true;
      }
    } catch (JWTVerificationException e) {
      log.debug("Access Token is invalid or expired: {}", e.getMessage());
    }

    if (!accessTokenOriginallyValid) {
      if (refreshToken != null) {
        try {
          DecodedJWT decodedRefreshToken = refreshTokenService.validateRefreshToken(refreshToken);
          UUID userId = UUID.fromString(decodedRefreshToken.getSubject());
          String newAccessToken =
              tokenService.generateAccessToken(userLookupService.getRequiredUser(userId));
          String newRefreshToken =
              refreshTokenService.createRefreshToken(userLookupService.getRequiredUser(userId));

          return new SessionValidationResult(
              SessionStatus.REFRESHED, newAccessToken, newRefreshToken, userId);
        } catch (IllegalArgumentException e) {
          log.warn("Refresh Token validation failed in policy: {}", e.getMessage());
          return new SessionValidationResult(SessionStatus.UNAUTHORIZED, null, null, null);
        }
      } else {
        log.debug("No refresh token available. Cannot validate/refresh session.");
        return new SessionValidationResult(SessionStatus.UNAUTHORIZED, null, null, null);
      }
    } else {
      return new SessionValidationResult(
          SessionStatus.VALID,
          accessToken,
          refreshToken,
          UUID.fromString(originalDecodedAccessToken.getSubject()));
    }
  }
}
