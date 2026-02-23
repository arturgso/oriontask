package br.com.oriontask.backend.auth.service;

import br.com.oriontask.backend.users.model.Users;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TokenService {

  @Value("${jwt.secret:change-me}")
  private String jwtSecret;

  @Value("${jwt.expMinutes:60}")
  private int expMinutes;

  @Value("${jwt.issuer}")
  private final String TOKEN_ISSUER = new String();

  public String generateToken(Users user) {
    log.debug("Generating token for userId={}", user.getId());
    Algorithm alg = Algorithm.HMAC256(jwtSecret);
    String token =
        JWT.create()
            .withIssuer(TOKEN_ISSUER)
            .withAudience("oriontask-web") // TODO - Trocar caso crie app
            .withSubject(user.getId().toString())
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Date.from(generateTokenExpiration()))
            .sign(alg);
    log.debug("Token generated for userId={} expMinutes={}", user.getId(), expMinutes);
    return token;
  }

  public String generateRefreshToken(Users user, Boolean rememberMe) {
    log.debug("Generating refresh token for userId={}", user.getId());
    Algorithm alg = Algorithm.HMAC256(jwtSecret);
    String token =
        JWT.create()
            .withIssuer(TOKEN_ISSUER)
            .withAudience("oriontask-refresh") // TODO - Trocar caso crie app
            .withSubject(user.getId().toString())
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Date.from((generateRefreshTokenExpiration(rememberMe))))
            .sign(alg);
    log.debug("Token generated for userId={} expMinutes={}", user.getId(), expMinutes);
    return token;
  }

  public Boolean validateToken(HttpServletRequest request) {
    String token = extractTokenFromRequest(request);
    if (token == null) {
      log.debug("Token validation failed: missing bearer token");
      return false;
    }

    try {
      if (verifyToken(token) != null) {
        log.debug("Token validated successfully");
        return true;
      }
    } catch (JWTVerificationException e) {
      log.debug("Token validation failed: {}", e.getMessage());
      return false;
    }

    log.debug("Token validation failed: unknown reason");
    return false;
  }

  /**
   * Validates and decodes JWT token
   *
   * @param token JWT token string
   * @return DecodedJWT if valid
   * @throws JWTVerificationException if invalid
   */
  public DecodedJWT verifyToken(String token) {
    Algorithm alg = Algorithm.HMAC256(jwtSecret);
    return JWT.require(alg)
        .withIssuer(TOKEN_ISSUER)
        .withAudience("oriontask-web")
        .build()
        .verify(token);
  }

  /**
   * Extracts userId from JWT token
   *
   * @param token JWT token string
   * @return UUID userId from subject claim
   */
  public UUID extractUserId(String token) {
    DecodedJWT decoded = verifyToken(token);
    log.debug("Extracted userId from token");
    return UUID.fromString(decoded.getSubject());
  }

  public String extractTokenFromRequest(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  private Instant generateTokenExpiration() {
    return LocalDateTime.now().plusMinutes(expMinutes).toInstant(ZoneOffset.of("-03:00"));
  }

  private Instant generateRefreshTokenExpiration(Boolean rememberMe) {
    return rememberMe
        ? LocalDateTime.now().plusWeeks(4).toInstant(ZoneOffset.of("-03:00"))
        : LocalDateTime.now().plusDays(2).toInstant(ZoneOffset.of("-03:00"));
  }
}
