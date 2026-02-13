package br.com.oriontask.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.oriontask.backend.dto.auth.AuthResponseDTO;
import br.com.oriontask.backend.dto.auth.LoginRequestDTO;
import br.com.oriontask.backend.dto.auth.SignupRequestDTO;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.UsersRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for AuthService. Tests cover: - Signup with valid data - Signup with disposable
 * email (blocked) - Username and email uniqueness - Login with username - Login with email -
 * Invalid credentials handling - JWT token generation with correct claims - Password hash
 * validation
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthServiceTest {

  @Autowired private AuthService authService;

  @Autowired private UsersRepository usersRepository;

  private String testUsername;
  private String testEmail;
  private String testPassword;

  @BeforeEach
  void setUp() {
    testUsername = "testuser" + System.currentTimeMillis();
    testEmail = "test" + System.currentTimeMillis() + "@example.com";
    testPassword = "Test123!@#";

    // Clean up any existing test data
    usersRepository.deleteAll();
  }

  @Test
  @DisplayName("Should create user successfully with valid data")
  void testSignupSuccess() {
    // Given
    SignupRequestDTO request =
        new SignupRequestDTO("Test User", testUsername, testEmail, testPassword);

    // When
    AuthResponseDTO response = authService.signup(request);

    // Then
    assertNotNull(response);
    assertNotNull(response.token());
    assertNotNull(response.id());
    assertEquals(testUsername, response.username());
    assertEquals("Test User", response.name());

    // Verify user is saved in database
    Optional<Users> savedUser = usersRepository.findByUsername(testUsername);
    assertTrue(savedUser.isPresent());
    assertEquals(testEmail, savedUser.get().getEmail());
  }

  @Test
  @DisplayName("Should generate JWT token with correct claims (sub=userId, username claim)")
  void testJwtTokenGeneration() {
    // Given
    SignupRequestDTO request =
        new SignupRequestDTO("JWT Test User", testUsername, testEmail, testPassword);

    // When
    AuthResponseDTO response = authService.signup(request);
    String token = response.token();

    // Then
    assertNotNull(token);

    // Decode token without verification to check claims
    DecodedJWT decoded = JWT.decode(token);

    // Verify subject is userId
    assertEquals(response.id().toString(), decoded.getSubject());

    // Verify username claim
    assertEquals(testUsername, decoded.getClaim("username").asString());

    // Verify token has expiration
    assertNotNull(decoded.getExpiresAt());
    assertNotNull(decoded.getIssuedAt());
  }

  @Test
  @DisplayName("Should hash password with bcrypt")
  void testPasswordHashing() {
    // Given
    SignupRequestDTO request =
        new SignupRequestDTO("Hash Test User", testUsername, testEmail, testPassword);

    // When
    authService.signup(request);

    // Then
    Users savedUser = usersRepository.findByUsername(testUsername).orElseThrow();
    String passwordHash = savedUser.getPasswordHash();

    // Verify hash is bcrypt format (starts with $2a$ or $2b$)
    assertTrue(
        passwordHash.startsWith("$2")
            || passwordHash.startsWith("$2a$")
            || passwordHash.startsWith("$2b$"));

    // Verify password matches hash
    assertTrue(BCrypt.checkpw(testPassword, passwordHash));

    // Verify wrong password doesn't match
    assertFalse(BCrypt.checkpw("WrongPassword123!", passwordHash));
  }

  @Test
  @DisplayName("Should reject disposable email domains")
  void testDisposableEmailBlocked() {
    // Given - List of disposable email domains that should be blocked
    String[] disposableDomains = {
      "mailinator.com",
      "10minutemail.com",
      "guerrillamail.com",
      "yopmail.com",
      "temp-mail.org",
      "fakemail.net",
      "trashmail.com"
    };

    for (String domain : disposableDomains) {
      String disposableEmail = "test@" + domain;
      SignupRequestDTO request =
          new SignupRequestDTO(
              "Disposable User",
              testUsername + domain.replace(".", ""),
              disposableEmail,
              testPassword);

      // When & Then
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> authService.signup(request),
              "Should reject disposable domain: " + domain);

      assertEquals("Disposable/temporary emails are not allowed", exception.getMessage());
    }
  }

  @Test
  @DisplayName("Should reject duplicate username")
  void testDuplicateUsername() {
    // Given - Create first user
    SignupRequestDTO firstRequest =
        new SignupRequestDTO("First User", testUsername, testEmail, testPassword);
    authService.signup(firstRequest);

    // When - Try to create second user with same username
    SignupRequestDTO duplicateRequest =
        new SignupRequestDTO(
            "Second User",
            testUsername, // Same username
            "different" + testEmail,
            testPassword);

    // Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.signup(duplicateRequest));
    assertEquals("Username unavailable", exception.getMessage());
  }

  @Test
  @DisplayName("Should reject duplicate email")
  void testDuplicateEmail() {
    // Given - Create first user
    SignupRequestDTO firstRequest =
        new SignupRequestDTO("First User", testUsername, testEmail, testPassword);
    authService.signup(firstRequest);

    // When - Try to create second user with same email
    SignupRequestDTO duplicateRequest =
        new SignupRequestDTO(
            "Second User",
            testUsername + "2",
            testEmail, // Same email
            testPassword);

    // Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.signup(duplicateRequest));
    assertEquals("Email unavailable", exception.getMessage());
  }

  @Test
  @DisplayName("Should login successfully with username")
  void testLoginWithUsername() {
    // Given - Create user
    SignupRequestDTO signupRequest =
        new SignupRequestDTO("Login Test User", testUsername, testEmail, testPassword);
    AuthResponseDTO signupResponse = authService.signup(signupRequest);

    // When - Login with username
    LoginRequestDTO loginRequest = new LoginRequestDTO(testUsername, testPassword);
    AuthResponseDTO loginResponse = authService.login(loginRequest);

    // Then
    assertNotNull(loginResponse);
    assertNotNull(loginResponse.token());
    assertEquals(signupResponse.id(), loginResponse.id());
    assertEquals(testUsername, loginResponse.username());
    assertEquals("Login Test User", loginResponse.name());
  }

  @Test
  @DisplayName("Should login successfully with email")
  void testLoginWithEmail() {
    // Given - Create user
    SignupRequestDTO signupRequest =
        new SignupRequestDTO("Email Login User", testUsername, testEmail, testPassword);
    AuthResponseDTO signupResponse = authService.signup(signupRequest);

    // When - Login with email
    LoginRequestDTO loginRequest = new LoginRequestDTO(testEmail, testPassword);
    AuthResponseDTO loginResponse = authService.login(loginRequest);

    // Then
    assertNotNull(loginResponse);
    assertNotNull(loginResponse.token());
    assertEquals(signupResponse.id(), loginResponse.id());
    assertEquals(testUsername, loginResponse.username());
    assertEquals("Email Login User", loginResponse.name());
  }

  @Test
  @DisplayName("Should reject login with non-existent username")
  void testLoginWithNonExistentUser() {
    // Given
    LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistent", testPassword);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    assertEquals("Invalid credentials", exception.getMessage());
  }

  @Test
  @DisplayName("Should reject login with wrong password")
  void testLoginWithWrongPassword() {
    // Given - Create user
    SignupRequestDTO signupRequest =
        new SignupRequestDTO("Wrong Password User", testUsername, testEmail, testPassword);
    authService.signup(signupRequest);

    // When - Try to login with wrong password
    LoginRequestDTO loginRequest = new LoginRequestDTO(testUsername, "WrongPassword123!");

    // Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    assertEquals("Invalid credentials", exception.getMessage());
  }

  @Test
  @DisplayName("Should generate different tokens for each login")
  void testDifferentTokensPerLogin() throws InterruptedException {
    // Given - Create user
    SignupRequestDTO signupRequest =
        new SignupRequestDTO("Token Test User", testUsername, testEmail, testPassword);
    authService.signup(signupRequest);

    // When - Login twice
    LoginRequestDTO loginRequest = new LoginRequestDTO(testUsername, testPassword);
    AuthResponseDTO firstLogin = authService.login(loginRequest);

    Thread.sleep(1000); // Wait to ensure different timestamp

    AuthResponseDTO secondLogin = authService.login(loginRequest);

    // Then - Tokens should be different (different iat)
    assertNotEquals(firstLogin.token(), secondLogin.token());

    // But user data should be the same
    assertEquals(firstLogin.id(), secondLogin.id());
    assertEquals(firstLogin.username(), secondLogin.username());
  }
}
