package br.com.oriontask.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.auth.dto.AuthResponseDTO;
import br.com.oriontask.backend.auth.dto.LoginRequestDTO;
import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.auth.service.AuthService;
import br.com.oriontask.backend.auth.service.TokenService;
import br.com.oriontask.backend.shared.service.EmailService;
import br.com.oriontask.backend.shared.service.RedisTokenService;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.mapper.UsersMapper;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UsersRepository usersRepository;
  @Mock private UsersMapper usersMapper;
  @Mock private TokenService jwtService;
  @Mock private EmailService emailService;
  @Mock private RedisTokenService redisTokenService;

  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("Should throw when email already exists on signup")
  void signupShouldThrowWhenEmailUnavailable() {
    SignupRequestDTO request = new SignupRequestDTO("Test User", "used@example.com", "Strong123!");

    when(usersRepository.findByEmail("used@example.com"))
        .thenReturn(Optional.of(Users.builder().id(UUID.randomUUID()).build()));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));

    assertEquals("Email unavailable", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
  }

  @Test
  @DisplayName("Should throw when signup email is disposable")
  void signupShouldThrowWhenEmailIsDisposable() {
    SignupRequestDTO request =
        new SignupRequestDTO("Test User", "test@mailinator.com", "Strong123!");

    when(usersRepository.findByEmail("test@mailinator.com")).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));

    assertEquals("Disposable/temporary emails are not allowed", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
  }

  @Test
  @DisplayName("Should save user with bcrypt hash and return mapped response on signup")
  void signupShouldHashPasswordAndReturnDTO() {
    UUID userId = UUID.randomUUID();
    SignupRequestDTO request = new SignupRequestDTO("Test User", "test@example.com", "Strong123!");
    UserResponseDTO expectedResponse =
        new UserResponseDTO(
            userId, "Test User", "test@example.com", false, new Timestamp(1), new Timestamp(2));

    when(usersRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(usersRepository.save(any(Users.class)))
        .thenAnswer(
            invocation -> {
              Users user = invocation.getArgument(0);
              user.setId(userId);
              return user;
            });
    when(usersMapper.toDTO(any(Users.class))).thenReturn(expectedResponse);

    UserResponseDTO result = authService.signup(request);

    assertNotNull(result);
    assertEquals(userId, result.id());
    assertEquals("test@example.com", result.email());
    verify(usersRepository).save(any(Users.class));
    verify(usersMapper).toDTO(any(Users.class));
  }

  @Test
  @DisplayName("Should send confirmation email on signup")
  void signupShouldSendConfirmationEmail() {
    UUID userId = UUID.randomUUID();
    SignupRequestDTO request = new SignupRequestDTO("Test User", "test@example.com", "Strong123!");

    when(usersRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(usersRepository.save(any(Users.class)))
        .thenAnswer(
            invocation -> {
              Users user = invocation.getArgument(0);
              user.setId(userId);
              return user;
            });
    when(usersMapper.toDTO(any(Users.class)))
        .thenReturn(
            new UserResponseDTO(
                userId,
                "Test User",
                "test@example.com",
                false,
                new Timestamp(1),
                new Timestamp(2)));

    authService.signup(request);

    verify(emailService).sendConfirmationEmail(eq("test@example.com"), any(String.class));
  }

  @Test
  @DisplayName("Should login with trimmed email and return token")
  void loginShouldTrimEmailAndReturnToken() {
    UUID userId = UUID.randomUUID();
    String rawEmail = "  test@example.com  ";
    String password = "Strong123!";
    String hash = BCrypt.hashpw(password, BCrypt.gensalt());

    Users user =
        Users.builder()
            .id(userId)
            .name("Test User")
            .email("test@example.com")
            .passwordHash(hash)
            .isConfirmed(true)
            .build();

    when(usersRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
    when(jwtService.generateToken(user)).thenReturn("jwt-token");

    AuthResponseDTO result = authService.login(new LoginRequestDTO(rawEmail, password));

    assertEquals("jwt-token", result.token());
    assertEquals(userId, result.id());
    verify(usersRepository).findByEmailIgnoreCase("test@example.com");
  }

  @Test
  @DisplayName("Should throw invalid credentials when email does not exist")
  void loginShouldThrowWhenUserNotFound() {
    when(usersRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequestDTO("unknown@example.com", "Strong123!")));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(jwtService, never()).generateToken(any(Users.class));
  }

  @Test
  @DisplayName("Should throw invalid credentials when password is wrong")
  void loginShouldThrowWhenPasswordIsInvalid() {
    Users user =
        Users.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .passwordHash(BCrypt.hashpw("Strong123!", BCrypt.gensalt()))
            .build();

    when(usersRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequestDTO("test@example.com", "Wrong123!")));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(jwtService, never()).generateToken(any(Users.class));
  }

  @Test
  @DisplayName("Should throw when email is not confirmed on login")
  void loginShouldThrowWhenEmailNotConfirmed() {
    UUID userId = UUID.randomUUID();
    String password = "Strong123!";
    String hash = BCrypt.hashpw(password, BCrypt.gensalt());

    Users user =
        Users.builder()
            .id(userId)
            .email("test@example.com")
            .passwordHash(hash)
            .isConfirmed(false)
            .build();

    when(usersRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequestDTO("test@example.com", password)));

    assertEquals("Please confirm your email before logging in.", exception.getMessage());
    verify(jwtService, never()).generateToken(any(Users.class));
  }

  @Test
  @DisplayName("Should confirm email when token is valid")
  void confirmEmailShouldWorkWhenTokenValid() {
    String token = "valid-token";
    Users user =
        Users.builder()
            .id(UUID.randomUUID())
            .isConfirmed(false)
            .confirmationToken(token)
            .confirmationTokenExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusHours(1)))
            .build();

    when(usersRepository.findByConfirmationToken(token)).thenReturn(Optional.of(user));

    authService.confirmEmail(token);

    org.junit.jupiter.api.Assertions.assertTrue(user.getIsConfirmed());
    org.junit.jupiter.api.Assertions.assertNull(user.getConfirmationToken());
    verify(usersRepository).save(user);
  }

  @Test
  @DisplayName("Should throw when confirmation token is invalid")
  void confirmEmailShouldThrowWhenTokenInvalid() {
    String token = "invalid-token";
    when(usersRepository.findByConfirmationToken(token)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.confirmEmail(token));

    assertEquals("Invalid confirmation token", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
  }

  @Test
  @DisplayName("Should throw when confirmation token is expired")
  void confirmEmailShouldThrowWhenTokenExpired() {
    String token = "expired-token";
    Users user =
        Users.builder()
            .id(UUID.randomUUID())
            .isConfirmed(false)
            .confirmationToken(token)
            .confirmationTokenExpiresAt(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)))
            .build();

    when(usersRepository.findByConfirmationToken(token)).thenReturn(Optional.of(user));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.confirmEmail(token));

    assertEquals("Confirmation token has expired", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
  }

  @Test
  @DisplayName("Should send password reset email if user exists")
  void forgotPassword_shouldSendEmailIfUserExists() {
    String email = "test@example.com";
    UUID userId = UUID.randomUUID();
    Users user = Users.builder().id(userId).email(email).build();
    String resetToken = "reset-token";

    when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(redisTokenService.storeToken(userId)).thenReturn(resetToken);

    authService.forgotPassword(email);

    verify(emailService).sendPasswordResetEmail(email, resetToken);
    verify(redisTokenService).storeToken(userId);
  }

  @Test
  @DisplayName("Should throw if forgotPassword email does not exist")
  void forgotPassword_shouldThrowIfEmailDoesNotExist() {
    String email = "nonexistent@example.com";

    when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.forgotPassword(email));

    assertEquals("User not found", exception.getMessage());
    verify(emailService, never()).sendPasswordResetEmail(any(), any());
    verify(redisTokenService, never()).storeToken(any());
  }

  @Test
  @DisplayName("Should reset password if token is valid")
  void resetPassword_shouldResetPasswordIfTokenValid() {
    String token = "valid-reset-token";
    String newPassword = "NewStrongPassword123!";
    UUID userId = UUID.randomUUID();
    Users user =
        Users.builder()
            .id(userId)
            .email("test@example.com")
            .passwordHash(BCrypt.hashpw("OldPassword123!", BCrypt.gensalt()))
            .isConfirmed(true)
            .build();

    when(redisTokenService.getUserIdFromToken(token)).thenReturn(Optional.of(userId));
    when(usersRepository.findById(userId)).thenReturn(Optional.of(user));

    authService.resetPassword(token, newPassword);

    verify(usersRepository)
        .save(argThat(savedUser -> BCrypt.checkpw(newPassword, savedUser.getPasswordHash())));
    verify(redisTokenService).invalidateToken(token);
  }

  @Test
  @DisplayName("Should throw if resetPassword token is invalid or expired")
  void resetPassword_shouldThrowIfTokenInvalidOrExpired() {
    String token = "invalid-reset-token";
    String newPassword = "NewStrongPassword123!";

    when(redisTokenService.getUserIdFromToken(token)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> authService.resetPassword(token, newPassword));

    assertEquals("Invalid or expired password reset token", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
    verify(redisTokenService, never()).invalidateToken(any());
  }

  @Test
  @DisplayName("Should throw if user does not exist for a valid reset token")
  void resetPassword_shouldThrowIfUserDoesNotExist() {
    String token = "valid-reset-token";
    String newPassword = "NewStrongPassword123!";
    UUID userId = UUID.randomUUID();

    when(redisTokenService.getUserIdFromToken(token)).thenReturn(Optional.of(userId));
    when(usersRepository.findById(userId)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> authService.resetPassword(token, newPassword));

    assertEquals("User not found", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
    verify(redisTokenService, never()).invalidateToken(any());
  }

  @Test
  @DisplayName("Should persist bcrypt-compatible hash on signup")
  void signupShouldPersistBcryptHash() {
    SignupRequestDTO request = new SignupRequestDTO("Test User", "test@example.com", "Strong123!");

    when(usersRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(usersRepository.save(any(Users.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(usersMapper.toDTO(any(Users.class)))
        .thenReturn(
            new UserResponseDTO(
                UUID.randomUUID(),
                "Test User",
                "test@example.com",
                false,
                new Timestamp(1),
                new Timestamp(2)));

    authService.signup(request);

    verify(usersRepository)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                user ->
                    user.getPasswordHash() != null
                        && BCrypt.checkpw("Strong123!", user.getPasswordHash())));
  }
}
