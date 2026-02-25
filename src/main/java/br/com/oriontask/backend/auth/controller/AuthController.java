package br.com.oriontask.backend.auth.controller;

import br.com.oriontask.backend.auth.dto.*;
import br.com.oriontask.backend.auth.service.AuthService;
import br.com.oriontask.backend.auth.service.TokenService;
import br.com.oriontask.backend.auth.utils.CookieUtils;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final TokenService tokenService;

  @PostMapping("signup")
  public ResponseEntity<UserResponseDTO> signup(@RequestBody @Validated SignupRequestDTO req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(req));
  }

  @PostMapping("confirm-email")
  public ResponseEntity<Void> confirmEmail(@RequestParam String token) {
    authService.confirmEmail(token);
    return ResponseEntity.ok().build();
  }

  @PostMapping("forgot-password")
  public ResponseEntity<Void> forgotPassword(@RequestBody @Validated ForgotPasswordRequestDTO req) {
    authService.forgotPassword(req.email());
    return ResponseEntity.ok().build();
  }

  @PostMapping("reset-password")
  public ResponseEntity<Void> resetPassword(@RequestBody @Validated ResetPasswordRequestDTO req) {
    authService.resetPassword(req.token(), req.newPassword());
    return ResponseEntity.ok().build();
  }

  @PostMapping("login")
  public ResponseEntity<AuthResponseDTO> login(
      @RequestBody @Validated LoginRequestDTO req, HttpServletResponse response) {
    Map<String, String> resp = authService.login(req);

    AuthResponseDTO responseDTO =
        new AuthResponseDTO(resp.get("token"), UUID.fromString(resp.get("id")));

    ResponseCookie cookie = CookieUtils.createRefreshTokenCookie(resp.get("refresh_token"));

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(responseDTO);
  }

  @PostMapping("logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    String token = tokenService.extractTokenFromRequest(request);
    try {
      tokenService.validateAccessToken(token);
      authService.logout(token);
    } catch (JWTVerificationException e) {
      log.warn("Attempt to logout with an invalid or expired access token: {}", e.getMessage());
    } finally {
      response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.clearRefreshTokenCookie().toString());
    }
    return ResponseEntity.noContent().build();
  }

  @PostMapping("validate")
  public ResponseEntity<AuthResponseDTO> validate(
      HttpServletRequest request, HttpServletResponse response) {
    String accessToken = tokenService.extractTokenFromRequest(request);
    String refreshToken = CookieUtils.getRefreshToken(request);

    SessionValidationResult result =
        authService.validateSessionAndRefresh(accessToken, refreshToken);

    switch (result.status()) {
      case VALID:
        return ResponseEntity.ok(new AuthResponseDTO(result.accessToken(), result.userId()));

      case REFRESHED:
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            CookieUtils.createRefreshTokenCookie(result.refreshToken()).toString());
        return ResponseEntity.ok(new AuthResponseDTO(result.accessToken(), result.userId()));

      case UNAUTHORIZED:
      default:
        response.addHeader(
            HttpHeaders.SET_COOKIE, CookieUtils.clearRefreshTokenCookie().toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
