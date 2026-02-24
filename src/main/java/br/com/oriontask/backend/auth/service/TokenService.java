package br.com.oriontask.backend.auth.service;

import br.com.oriontask.backend.users.model.Users;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;

public interface TokenService {
  String generateAccessToken(Users user);

  String generateRefreshToken(Users user);

  DecodedJWT validateAccessToken(String token);

  DecodedJWT validateRefreshToken(String token);

  String extractTokenFromRequest(HttpServletRequest request);
}
