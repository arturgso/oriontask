package br.com.oriontask.backend.service;

import br.com.oriontask.backend.dto.auth.AuthResponseDTO;
import br.com.oriontask.backend.dto.auth.LoginRequestDTO;
import br.com.oriontask.backend.dto.auth.SignupRequestDTO;
import br.com.oriontask.backend.dto.users.UserResponseDTO;
import br.com.oriontask.backend.mappers.UsersMapper;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.UsersRepository;
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
    usersRepository
        .findByUsername(req.username())
        .ifPresent(
            u -> {
              throw new IllegalArgumentException("Username unavailable");
            });
    usersRepository
        .findByEmail(req.email())
        .ifPresent(
            u -> {
              throw new IllegalArgumentException("Email unavailable");
            });

    if (isDisposableEmail(req.email())) {
      throw new IllegalArgumentException("Disposable/temporary emails are not allowed");
    }

    String passwordHash = BCrypt.hashpw(req.password(), BCrypt.gensalt());

    Users user =
        Users.builder()
            .name(req.name())
            .username(req.username())
            .email(req.email())
            .passwordHash(passwordHash)
            .build();

    user = usersRepository.save(user);
    return usersMapper.toDTO(user);
  }

  public AuthResponseDTO login(LoginRequestDTO req) {
    String login = req.login().trim();
    Optional<Users> userOpt = usersRepository.findByEmailIgnoreCaseOrUsername(login, login);

    Users user = userOpt.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!BCrypt.checkpw(req.password(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String token = jwtService.generateToken(user);
    return new AuthResponseDTO(token, user.getId(), user.getUsername());
  }

  private boolean isDisposableEmail(String email) {
    String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
    return DISPOSABLE_DOMAINS.contains(domain);
  }
}
