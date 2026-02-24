package br.com.oriontask.backend.auth.service;

import br.com.oriontask.backend.auth.dto.LoginRequestDTO;
import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.auth.policy.AuthPolicy;
import br.com.oriontask.backend.refreshtoken.service.RefreshTokenService;
import br.com.oriontask.backend.shared.service.EmailService;
import br.com.oriontask.backend.shared.service.RedisTokenService;
import br.com.oriontask.backend.shared.utils.UserLookupService;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.mapper.UsersMapper;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final UsersRepository usersRepository;
  private final UsersMapper usersMapper;
  private final EmailService emailService;
  private final TokenService tokenService;
  private final RefreshTokenService refreshTokenService;
  private final RedisTokenService redisTokenService;
  private final AuthPolicy authPolicy;
  private final UserLookupService userLookupService;

  // minimal disposable email domain list; configurable via property in future
  private static final Set<String> DISPOSABLE_DOMAINS =
      new HashSet<>(
          Arrays.asList(
              "mailinator.com",
              "10minutemail.com",
              "guerrillamail.com",
              "yopmail.com",
              "temp-mail.org",
              "fakemail.net",
              "trashmail.com"));

  @Transactional
  public UserResponseDTO signup(SignupRequestDTO req) {
    String email = req.email().toLowerCase();

    log.info("Signup requested for email={}", email);

    if (isDisposableEmail(email)) {
      log.warn("Signup blocked: disposable email detected email={}", email);
      throw new IllegalArgumentException("Disposable/temporary emails are not allowed");
    }

    if (userLookupService.existsByEmail(email)) {
      log.warn("Signup blocked: email unavailable email={}", email);
      throw new IllegalArgumentException("Email unavailable");
    }

    String passwordHash = BCrypt.hashpw(req.password(), BCrypt.gensalt());

    String confirmationToken = UUID.randomUUID().toString();
    Timestamp expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(24));

    Users user = usersMapper.toEntity(req);

    user.setPasswordHash(passwordHash);
    user.setConfirmationToken(confirmationToken);
    user.setIsConfirmed(false);
    user.setConfirmationTokenExpiresAt(expiresAt);

    user = usersRepository.save(user);
    emailService.sendConfirmationEmail(user.getEmail(), confirmationToken);

    log.info("Signup completed userId={} email={}", user.getId(), user.getEmail());
    return usersMapper.toDTO(user);
  }

  @Transactional
  public void confirmEmail(String token) {
    log.info("Confirming email for token={}", token);
    Users user =
        usersRepository
            .findByConfirmationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid confirmation token"));

    if (user.getConfirmationTokenExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
      log.warn("Confirmation token expired for userId={}", user.getId());
      throw new IllegalArgumentException("Confirmation token has expired");
    }

    user.setIsConfirmed(true);
    user.setConfirmationToken(null);
    user.setConfirmationTokenExpiresAt(null);
    usersRepository.save(user);
    log.info("Email confirmed for userId={}", user.getId());
  }

  public Map<String, String> login(LoginRequestDTO req) {
    String email = req.email().trim();
    log.info("Login requested");

    Users user = userLookupService.getByEmail(email);
    if (user == null) {
      log.warn("Login failed: user not found");
      throw new IllegalArgumentException("Invalid credentials");
    }

    authPolicy.verifyPasswordHash(req.password(), user.getPasswordHash());
    authPolicy.isEmailConfirmed(user.getIsConfirmed());

    String token = tokenService.generateAccessToken(user);
    String refreshToken = tokenService.generateRefreshToken(user);

    refreshTokenService.createRefreshToken(refreshToken, user);

    log.info("Login succeeded userId={}", user.getId());

    Map<String, String> res = new HashMap<>();
    res.put("token", token);
    res.put("id", user.getId().toString());
    res.put("refresh_token", refreshToken);

    return res;
  }

  @Transactional
  public void forgotPassword(String email) {
    log.info("Forgot password requested for email={}", email);
    Users user =
        usersRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new IllegalArgumentException("User not found")); // Generic error for security

    String token = redisTokenService.createPasswordResetToken(user.getId());
    emailService.sendPasswordResetEmail(user.getEmail(), token);
    log.info("Password reset email sent for userId={}", user.getId());
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
    log.info("Password reset requested with token={}", token);
    UUID userId =
        redisTokenService
            .getUserIdByResetToken(token)
            .orElseThrow(
                () -> new IllegalArgumentException("Invalid or expired password reset token"));

    Users user =
        usersRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
    usersRepository.save(user);
    redisTokenService.deletePasswordResetToken(token);
    log.info("Password reset successfully for userId={}", userId);
  }

  public void logout(String token) {
    log.info("Logout requested");
    redisTokenService.blacklistToken(token);
  }

  private boolean isDisposableEmail(String email) {
    String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
    return DISPOSABLE_DOMAINS.contains(domain);
  }
}
