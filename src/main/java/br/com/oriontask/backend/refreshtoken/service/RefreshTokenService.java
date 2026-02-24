package br.com.oriontask.backend.refreshtoken.service;

import br.com.oriontask.backend.auth.service.TokenService;
import br.com.oriontask.backend.refreshtoken.models.RefreshToken;
import br.com.oriontask.backend.refreshtoken.repository.RefreshTokenRepository;
import br.com.oriontask.backend.refreshtoken.utils.HashUtils;
import br.com.oriontask.backend.users.model.Users;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenService tokenService;

  public String createRefreshToken(Users user) {
    String refresh = tokenService.generateRefreshToken(user);

    RefreshToken refreshToken =
        RefreshToken.builder()
            .user(user)
            .tokenHash(HashUtils.sha256(refresh))
            .expirationAt(
                new Timestamp(System.currentTimeMillis())) // TODO - Change to real expire at
            .build();

    refreshTokenRepository.save(refreshToken);
    return refresh;
  }

  public void validateRefreshToken(String refreshToken) {}
}
