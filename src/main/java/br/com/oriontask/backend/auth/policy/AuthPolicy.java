package br.com.oriontask.backend.auth.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthPolicy {

  private final PasswordEncoder passwordEncoder;

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
}
