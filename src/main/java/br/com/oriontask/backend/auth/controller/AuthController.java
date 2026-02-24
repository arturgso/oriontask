package br.com.oriontask.backend.auth.controller;

import br.com.oriontask.backend.auth.dto.AuthResponseDTO;
import br.com.oriontask.backend.auth.dto.ForgotPasswordRequestDTO;
import br.com.oriontask.backend.auth.dto.LoginRequestDTO;
import br.com.oriontask.backend.auth.dto.ResetPasswordRequestDTO;
import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.auth.service.AuthService;
import br.com.oriontask.backend.auth.service.TokenService;
import br.com.oriontask.backend.auth.utils.CookieUtils;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

    ResponseCookie cookie =
        ResponseCookie.from("refresh_token", resp.get("refresh_token"))
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(7))
            .sameSite("Lax")
            .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(responseDTO);
  }

  @PostMapping("logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String token = tokenService.extractTokenFromRequest(request);
    tokenService.validateAccessToken(token);
    authService.logout(token);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("validate")
  public ResponseEntity<Void> validate(HttpServletRequest request) {
    String refreshToken = CookieUtils.getRefreshToken(request);
    String accessToken = tokenService.extractTokenFromRequest(request);
    DecodedJWT result = tokenService.validateAccessToken(accessToken);

    if (result != null) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.status(401).build();
    }
  }
}
