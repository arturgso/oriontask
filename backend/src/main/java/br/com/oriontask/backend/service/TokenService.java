package br.com.oriontask.backend.service;

import br.com.oriontask.backend.model.Users;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

  public String generateToken(Users user) {
    Algorithm alg = Algorithm.HMAC256(jwtSecret);
    Instant now = Instant.now();
    return JWT.create()
            .withSubject(user.getId().toString())
            .withClaim("username", user.getUsername())
            .withIssuedAt(java.util.Date.from(now))
            .withExpiresAt(java.util.Date.from(now.plus(expMinutes, ChronoUnit.MINUTES)))
            .sign(alg);
  }

  public Boolean validateToken(HttpServletRequest request) {
    String token = extractTokenFromRequest(request);

    try {
      if (verifyToken(token) != null) {
        log.debug("Valid Token");
        return true;
      }
    } catch (JWTVerificationException e) {
      log.debug("Invalid Token {}", e);
      return false;
    }

    log.debug("Invalid Token");
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
    return JWT.require(alg).build().verify(token);
  }

  /**
   * Extracts userId from JWT token
   *
   * @param token JWT token string
   * @return UUID userId from subject claim
   */
  public UUID extractUserId(String token) {
    DecodedJWT decoded = verifyToken(token);
    return UUID.fromString(decoded.getSubject());
  }

  /**
   * Extracts username from JWT token
   *
   * @param token JWT token string
   * @return username from custom claim
   */
  public String extractUsername(String token) {
    DecodedJWT decoded = verifyToken(token);
    return decoded.getClaim("username").asString();
  }

  public String extractTokenFromRequest(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}
