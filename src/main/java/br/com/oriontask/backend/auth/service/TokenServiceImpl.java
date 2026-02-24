package br.com.oriontask.backend.auth.service;

import br.com.oriontask.backend.users.model.Users;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
public class TokenServiceImpl implements TokenService {

  @Value("${jwt.secret:change-me}")
  private String jwtSecret;

  @Value("${jwt.expMinutes:60}")
  private int expMinutes;

  @Value("${jwt.issuer}")
  private String tokenIssuer;

  @Override
  public String generateAccessToken(Users user) {
    log.debug("Generating access token for user {}", user.getId());
    return createToken(user, getAccessTokenExpiration(), "oriontask-access");
  }

  @Override
  public String generateRefreshToken(Users user) {
    log.debug("Generating refresh token for user {}", user.getId());
    return createToken(user, getRefreshTokenExpiration(), "oriontask-refresh");
  }

  @Override
  public DecodedJWT validateAccessToken(String token) {
    return genericTokenValidation(token, "oriontask-access");
  }

  @Override
  public DecodedJWT validateRefreshToken(String token) {
    return genericTokenValidation(token, "oriontask-refresh");
  }

  @Override
  public String extractTokenFromRequest(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  private String createToken(Users user, Instant expiration, String audience) {
    log.debug("Generating token for userId={}", user.getId());
    Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

    String token =
        JWT.create()
            .withIssuer(tokenIssuer)
            .withAudience(audience)
            .withSubject(user.getId().toString())
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(expiration))
            .sign(algorithm);
    log.debug("Token generated for userId={} expMinutes={}", user.getId(), expMinutes);
    return token;
  }

  private DecodedJWT genericTokenValidation(String token, String expectedAudience) {
    Algorithm alg = Algorithm.HMAC256(jwtSecret);
    return JWT.require(alg)
        .withIssuer(tokenIssuer)
        .withAudience(expectedAudience)
        .build()
        .verify(token);
  }

  private Instant getAccessTokenExpiration() {
    return LocalDateTime.now().plusMinutes(expMinutes).toInstant(ZoneOffset.of("-03:00"));
  }

  private Instant getRefreshTokenExpiration() {
    return LocalDateTime.now().plusDays(7).toInstant(ZoneOffset.of("-03:00"));
  }
}
