package br.com.oriontask.backend.controller;

import br.com.oriontask.backend.dto.auth.AuthResponseDTO;
import br.com.oriontask.backend.dto.auth.LoginRequestDTO;
import br.com.oriontask.backend.dto.auth.SignupRequestDTO;
import br.com.oriontask.backend.dto.users.UserResponseDTO;
import br.com.oriontask.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("signup")
  public ResponseEntity<UserResponseDTO> signup(@RequestBody @Validated SignupRequestDTO req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(req));
  }

  @PostMapping("login")
  public ResponseEntity<AuthResponseDTO> login(
      @RequestBody @Validated LoginRequestDTO req, HttpServletRequest request) {
    AuthResponseDTO resp = authService.login(req);
    return ResponseEntity.ok().body(resp);
  }

  @PostMapping("logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    ResponseCookie uid = buildCookie("uid", "", request, true);
    ResponseCookie uname = buildCookie("uname", "", request, true);
    return ResponseEntity.ok()
        .header("Set-Cookie", uid.toString())
        .header("Set-Cookie", uname.toString())
        .build();
  }

  @PostMapping("validate")
  public ResponseEntity<Void> validate(HttpServletRequest request) {
    Boolean result = authService.validateToken(request);

    if (result) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.status(401).build();
    }
  }

  private static ResponseCookie buildCookie(
      String name, String value, HttpServletRequest request, boolean clear) {
    ResponseCookie.ResponseCookieBuilder builder =
        ResponseCookie.from(name, value).sameSite("None").path("/").secure(true);
    if (clear) {
      builder.maxAge(0);
    }
    return builder.build();
  }
}
