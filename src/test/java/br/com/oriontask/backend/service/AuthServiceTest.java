package br.com.oriontask.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.auth.dto.AuthResponseDTO;
import br.com.oriontask.backend.auth.dto.LoginRequestDTO;
import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.auth.service.AuthService;
import br.com.oriontask.backend.auth.service.TokenService;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.mapper.UsersMapper;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import java.sql.Timestamp;
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

  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("Should throw when username already exists on signup")
  void signupShouldThrowWhenUsernameUnavailable() {
    SignupRequestDTO request =
        new SignupRequestDTO("Test User", "taken_user", "test@example.com", "Strong123!");

    when(usersRepository.findByUsername("taken_user"))
        .thenReturn(Optional.of(Users.builder().id(UUID.randomUUID()).build()));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));

    assertEquals("Username unavailable", exception.getMessage());
    verify(usersRepository, never()).save(any(Users.class));
  }

  @Test
  @DisplayName("Should throw when email already exists on signup")
  void signupShouldThrowWhenEmailUnavailable() {
    SignupRequestDTO request =
        new SignupRequestDTO("Test User", "available_user", "used@example.com", "Strong123!");

    when(usersRepository.findByUsername("available_user")).thenReturn(Optional.empty());
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
        new SignupRequestDTO("Test User", "available_user", "test@mailinator.com", "Strong123!");

    when(usersRepository.findByUsername("available_user")).thenReturn(Optional.empty());
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
    SignupRequestDTO request =
        new SignupRequestDTO("Test User", "test_user", "test@example.com", "Strong123!");
    UserResponseDTO expectedResponse =
        new UserResponseDTO(
            userId,
            "Test User",
            "test_user",
            "test@example.com",
            new Timestamp(1),
            new Timestamp(2));

    when(usersRepository.findByUsername("test_user")).thenReturn(Optional.empty());
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
    assertEquals("test_user", result.username());

    verify(usersRepository).save(any(Users.class));
    verify(usersMapper).toDTO(any(Users.class));
  }

  @Test
  @DisplayName("Should login with trimmed login and return token")
  void loginShouldTrimLoginAndReturnToken() {
    UUID userId = UUID.randomUUID();
    String rawLogin = "  test_user  ";
    String password = "Strong123!";
    String hash = BCrypt.hashpw(password, BCrypt.gensalt());

    Users user =
        Users.builder()
            .id(userId)
            .name("Test User")
            .username("test_user")
            .email("test@example.com")
            .passwordHash(hash)
            .build();

    when(usersRepository.findByEmailIgnoreCaseOrUsername("test_user", "test_user"))
        .thenReturn(Optional.of(user));
    when(jwtService.generateToken(user)).thenReturn("jwt-token");

    AuthResponseDTO result = authService.login(new LoginRequestDTO(rawLogin, password));

    assertEquals("jwt-token", result.token());
    assertEquals(userId, result.id());
    assertEquals("test_user", result.username());
    verify(usersRepository).findByEmailIgnoreCaseOrUsername("test_user", "test_user");
  }

  @Test
  @DisplayName("Should throw invalid credentials when login does not exist")
  void loginShouldThrowWhenUserNotFound() {
    when(usersRepository.findByEmailIgnoreCaseOrUsername("unknown", "unknown"))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequestDTO("unknown", "Strong123!")));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(jwtService, never()).generateToken(any(Users.class));
  }

  @Test
  @DisplayName("Should throw invalid credentials when password is wrong")
  void loginShouldThrowWhenPasswordIsInvalid() {
    Users user =
        Users.builder()
            .id(UUID.randomUUID())
            .username("test_user")
            .email("test@example.com")
            .passwordHash(BCrypt.hashpw("Strong123!", BCrypt.gensalt()))
            .build();

    when(usersRepository.findByEmailIgnoreCaseOrUsername("test_user", "test_user"))
        .thenReturn(Optional.of(user));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequestDTO("test_user", "Wrong123!")));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(jwtService, never()).generateToken(any(Users.class));
  }

  @Test
  @DisplayName("Should persist bcrypt-compatible hash on signup")
  void signupShouldPersistBcryptHash() {
    SignupRequestDTO request =
        new SignupRequestDTO("Test User", "test_user", "test@example.com", "Strong123!");

    when(usersRepository.findByUsername("test_user")).thenReturn(Optional.empty());
    when(usersRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(usersRepository.save(any(Users.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(usersMapper.toDTO(any(Users.class)))
        .thenReturn(
            new UserResponseDTO(
                UUID.randomUUID(),
                "Test User",
                "test_user",
                "test@example.com",
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
