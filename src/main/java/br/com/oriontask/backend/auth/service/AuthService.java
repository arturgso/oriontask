package br.com.oriontask.backend.auth.service;

import br.com.oriontask.backend.auth.dto.AuthResponseDTO;
import br.com.oriontask.backend.auth.dto.LoginRequestDTO;
import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.mapper.UsersMapper;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import jakarta.transaction.Transactional;
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

  private final TokenService jwtService;

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
    log.info("Signup requested for email={}", req.email());
    usersRepository
        .findByEmail(req.email())
        .ifPresent(
            u -> {
              log.warn("Signup blocked: email unavailable email={}", req.email());
              throw new IllegalArgumentException("Email unavailable");
            });

    if (isDisposableEmail(req.email())) {
      log.warn("Signup blocked: disposable email detected email={}", req.email());
      throw new IllegalArgumentException("Disposable/temporary emails are not allowed");
    }

    String passwordHash = BCrypt.hashpw(req.password(), BCrypt.gensalt());

    Users user =
        Users.builder().name(req.name()).email(req.email()).passwordHash(passwordHash).build();

    user = usersRepository.save(user);
    log.info("Signup completed userId={} email={}", user.getId(), user.getEmail());
    return usersMapper.toDTO(user);
  }

  public AuthResponseDTO login(LoginRequestDTO req) {
    String email = req.email().trim();
    log.info("Login requested");
    Optional<Users> userOpt = usersRepository.findByEmailIgnoreCase(email);

    Users user =
        userOpt.orElseThrow(
            () -> {
              log.warn("Login failed: user not found");
              return new IllegalArgumentException("Invalid credentials");
            });

    if (!BCrypt.checkpw(req.password(), user.getPasswordHash())) {
      log.warn("Login failed: invalid password userId={}", user.getId());
      throw new IllegalArgumentException("Invalid credentials");
    }

    String token = jwtService.generateToken(user);
    log.info("Login succeeded userId={}", user.getId());
    return new AuthResponseDTO(token, user.getId());
  }

  private boolean isDisposableEmail(String email) {
    String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
    return DISPOSABLE_DOMAINS.contains(domain);
  }
}
