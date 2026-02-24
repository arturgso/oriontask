package br.com.oriontask.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.com.oriontask.backend.auth.dto.LoginRequestDTO;
import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.auth.policy.AuthPolicy;
import br.com.oriontask.backend.auth.service.AuthService;
import br.com.oriontask.backend.auth.service.TokenService;
import br.com.oriontask.backend.refreshtoken.service.RefreshTokenService;
import br.com.oriontask.backend.shared.service.EmailService;
import br.com.oriontask.backend.shared.service.RedisTokenService;
import br.com.oriontask.backend.shared.utils.UserLookupService;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.mapper.UsersMapper;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
  @Mock private TokenService tokenService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private EmailService emailService;
  @Mock private RedisTokenService redisTokenService;
  @Mock private AuthPolicy authPolicy;
  @Mock private UserLookupService userLookupService;

  @InjectMocks private AuthService authService;

  private UUID testUserId;
  private String testName;
  private String testEmail;
  private String testPassword;
  private SignupRequestDTO defaultSignupRequest;
  private Users testUser;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testName = "Test User";
    testEmail = "test@example.com";
    testPassword = "$2a$12$NRtXVb4W/KTxsG.tgmE4Iu4V83JuMbO/LgOo221bCpiPSoK5Qh/jG";
    defaultSignupRequest = new SignupRequestDTO(testName, testEmail, testPassword);
    testUser =
        Users.builder()
            .id(testUserId)
            .name(testName)
            .email(testEmail)
            .passwordHash(testPassword)
            .isConfirmed(true)
            .build();
  }

  @Test
  @DisplayName("Should throw when email already exists on signup")
  void signupShouldThrowWhenEmailUnavailable() {
    SignupRequestDTO request = new SignupRequestDTO("Test User", "used@example.com", "Strong123!");

    when(userLookupService.existsByEmail("used@example.com")).thenReturn(true);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));

    assertEquals("Email unavailable", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
  }

  @Test
  @DisplayName("Should throw when signup email is disposable")
  void signupShouldThrowWhenEmailIsDisposable() {
    SignupRequestDTO request = new SignupRequestDTO(testName, "test@mailinator.com", testPassword);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));

    assertEquals("Disposable/temporary emails are not allowed", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
    verify(userLookupService, never()).getByEmail("test@mailinator.com");
  }

  @Test
  @DisplayName("Should save user with bcrypt hash and return mapped response on signup")
  void signupShouldHashPasswordAndReturnDTO() {
    UserResponseDTO expectedResponse =
        new UserResponseDTO(
            testUserId, testName, testEmail, false, new Timestamp(1), new Timestamp(2));

    when(userLookupService.existsByEmail(testEmail)).thenReturn(false);
    when(usersMapper.toEntity(any(SignupRequestDTO.class)))
        .thenReturn(Users.builder().email(testEmail).build());
    when(usersRepository.save(any(Users.class)))
        .thenAnswer(
            invocation -> {
              Users user = invocation.getArgument(0);
              user.setId(testUserId);
              return user;
            });
    when(usersMapper.toDTO(any(Users.class))).thenReturn(expectedResponse);

    UserResponseDTO result = authService.signup(defaultSignupRequest);

    assertNotNull(result);
    assertEquals(testUserId, result.id());
    assertEquals(testEmail, result.email());
    verify(usersRepository).save(any(Users.class));
    verify(usersMapper).toDTO(any(Users.class));
  }

  @Test
  @DisplayName("Should send confirmation email on signup")
  void signupShouldSendConfirmationEmail() {
    when(userLookupService.existsByEmail(testEmail)).thenReturn(false);
    when(usersMapper.toEntity(any(SignupRequestDTO.class)))
        .thenReturn(Users.builder().email(testEmail).build());
    when(usersRepository.save(any(Users.class)))
        .thenAnswer(
            invocation -> {
              Users user = invocation.getArgument(0);
              user.setId(testUserId);
              return user;
            });
    when(usersMapper.toDTO(any(Users.class)))
        .thenReturn(
            new UserResponseDTO(
                testUserId, testName, testEmail, false, new Timestamp(1), new Timestamp(2)));

    authService.signup(defaultSignupRequest);

    verify(emailService).sendConfirmationEmail(eq(testEmail), any(String.class));
  }

  @Test
  @DisplayName("Should login with trimmed email and return token")
  void loginShouldTrimEmailAndReturnToken() {
    String rawEmail = "  " + testEmail + "  ";

    when(userLookupService.getByEmail(testEmail)).thenReturn(testUser);
    when(tokenService.generateAccessToken(testUser)).thenReturn("jwt-token");
    when(tokenService.generateRefreshToken(testUser)).thenReturn("refresh-token");
    when(refreshTokenService.createRefreshToken(eq("refresh-token"), eq(testUser)))
        .thenReturn("refresh-token");

    java.util.Map<String, String> result =
        authService.login(new LoginRequestDTO(rawEmail, testPassword));

    assertEquals("jwt-token", result.get("token"));
    assertEquals(testUserId.toString(), result.get("id"));
    assertEquals("refresh-token", result.get("refresh_token"));
    verify(userLookupService).getByEmail(testEmail);
  }

  @Test
  @DisplayName("Should throw invalid credentials when email does not exist")
  void loginShouldThrowWhenUserNotFound() {
    when(userLookupService.getByEmail("unknown@example.com")).thenReturn(null);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequestDTO("unknown@example.com", "Strong123!")));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(tokenService, never()).generateAccessToken(any(Users.class));
  }

  @Test
  @DisplayName("Should throw invalid credentials when password is wrong")
  void loginShouldThrowWhenPasswordIsInvalid() {
    Users user =
        Users.builder()
            .id(UUID.randomUUID())
            .email(testEmail)
            .passwordHash("$2a$12$NRtXVb4W/KTxsG.tgmE4Iu4V83JuMbO/LgOo221bCpiPSoK5Qh/jG")
            .build();

    when(userLookupService.getByEmail(testEmail)).thenReturn(user);
    doThrow(new IllegalArgumentException("Invalid credentials"))
        .when(authPolicy)
        .verifyPasswordHash(eq("Wrong123!"), anyString());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequestDTO(testEmail, "Wrong123!")));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(tokenService, never()).generateAccessToken(any(Users.class));
  }

  @Test
  @DisplayName("Should throw when email is not confirmed on login")
  void loginShouldThrowWhenEmailNotConfirmed() {
    Users user =
        Users.builder()
            .id(testUserId)
            .email(testEmail)
            .passwordHash("$2a$12$NRtXVb4W/KTxsG.tgmE4Iu4V83JuMbO/LgOo221bCpiPSoK5Qh/jG")
            .isConfirmed(false)
            .build();

    when(userLookupService.getByEmail(testEmail)).thenReturn(user);
    doThrow(new IllegalArgumentException("Please confirm your email before logging in."))
        .when(authPolicy)
        .isEmailConfirmed(user.getIsConfirmed());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequestDTO(testEmail, testPassword)));

    assertEquals("Please confirm your email before logging in.", exception.getMessage());
    verify(tokenService, never()).generateAccessToken(any(Users.class));
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
    Users user = Users.builder().id(testUserId).email(testEmail).build();
    String resetToken = "reset-token";

    when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));
    when(redisTokenService.createPasswordResetToken(testUserId)).thenReturn(resetToken);

    authService.forgotPassword(testEmail);

    verify(emailService).sendPasswordResetEmail(testEmail, resetToken);
    verify(redisTokenService).createPasswordResetToken(testUserId);
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
    verify(redisTokenService, never()).createPasswordResetToken(any());
  }

  @Test
  @DisplayName("Should reset password if token is valid")
  void resetPassword_shouldResetPasswordIfTokenValid() {
    String token = "valid-reset-token";
    String newPassword = "NewStrongPassword123!";
    Users user =
        Users.builder()
            .id(testUserId)
            .email(testEmail)
            .passwordHash(BCrypt.hashpw("OldPassword123!", BCrypt.gensalt()))
            .isConfirmed(true)
            .build();

    when(redisTokenService.getUserIdByResetToken(token)).thenReturn(Optional.of(testUserId));
    when(usersRepository.findById(testUserId)).thenReturn(Optional.of(user));

    authService.resetPassword(token, newPassword);

    verify(usersRepository)
        .save(argThat(savedUser -> BCrypt.checkpw(newPassword, savedUser.getPasswordHash())));
    verify(redisTokenService).deletePasswordResetToken(token);
  }

  @Test
  @DisplayName("Should throw if resetPassword token is invalid or expired")
  void resetPassword_shouldThrowIfTokenInvalidOrExpired() {
    String token = "invalid-reset-token";
    String newPassword = "NewStrongPassword123!";

    when(redisTokenService.getUserIdByResetToken(token)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> authService.resetPassword(token, newPassword));

    assertEquals("Invalid or expired password reset token", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
    verify(redisTokenService, never()).deletePasswordResetToken(any());
  }

  @Test
  @DisplayName("Should throw if user does not exist for a valid reset token")
  void resetPassword_shouldThrowIfUserDoesNotExist() {
    String token = "valid-reset-token";
    String newPassword = "NewStrongPassword123!";
    UUID userId = UUID.randomUUID();

    when(redisTokenService.getUserIdByResetToken(token)).thenReturn(Optional.of(userId));
    when(usersRepository.findById(userId)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> authService.resetPassword(token, newPassword));

    assertEquals("User not found", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
    verify(redisTokenService, never()).deletePasswordResetToken(any());
  }

  @Test
  @DisplayName("Should persist bcrypt-compatible hash on signup")
  void signupShouldPersistBcryptHash() {
    when(usersMapper.toEntity(any(SignupRequestDTO.class)))
        .thenReturn(Users.builder().email(testEmail).build());
    when(usersRepository.save(any(Users.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(usersMapper.toDTO(any(Users.class)))
        .thenReturn(
            new UserResponseDTO(
                testUserId, testName, testEmail, false, new Timestamp(1), new Timestamp(2)));

    authService.signup(defaultSignupRequest);

    verify(usersRepository)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                user ->
                    user.getPasswordHash() != null
                        && BCrypt.checkpw(testPassword, user.getPasswordHash())));
  }
}
