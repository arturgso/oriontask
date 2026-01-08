package br.com.oriontask.backend.utils;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtils {

    @Value("${jwt.secret:change-me}")
    private String jwtSecret;

    /**
     * Validates and decodes JWT token
     * @param token JWT token string
     * @return DecodedJWT if valid
     * @throws JWTVerificationException if invalid
     */
    public DecodedJWT validateToken(String token) {
        Algorithm alg = Algorithm.HMAC256(jwtSecret);
        return JWT.require(alg).build().verify(token);
    }

    /**
     * Extracts userId from JWT token
     * @param token JWT token string
     * @return UUID userId from subject claim
     */
    public UUID extractUserId(String token) {
        DecodedJWT decoded = validateToken(token);
        return UUID.fromString(decoded.getSubject());
    }

    /**
     * Extracts username from JWT token
     * @param token JWT token string
     * @return username from custom claim
     */
    public String extractUsername(String token) {
        DecodedJWT decoded = validateToken(token);
        return decoded.getClaim("username").asString();
    }
}

