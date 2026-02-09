package br.com.oriontask.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.oriontask.backend.dto.AuthResponseDTO;
import br.com.oriontask.backend.dto.LoginRequestDTO;
import br.com.oriontask.backend.dto.SignupRequestDTO;
import br.com.oriontask.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("signup")
    public ResponseEntity<AuthResponseDTO> signup(@RequestBody @Validated SignupRequestDTO req) {
        AuthResponseDTO resp = authService.signup(req);
        // Set minimal cookies with id and username
        ResponseCookie uid = ResponseCookie.from("uid", resp.id().toString())
                .sameSite("Lax").path("/").build();
        ResponseCookie uname = ResponseCookie.from("uname", resp.username())
                .sameSite("Lax").path("/").build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie", uid.toString())
                .header("Set-Cookie", uname.toString())
                .body(resp);
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Validated LoginRequestDTO req) {
        AuthResponseDTO resp = authService.login(req);
        ResponseCookie uid = ResponseCookie.from("uid", resp.id().toString())
                .sameSite("Lax").path("/").build();
        ResponseCookie uname = ResponseCookie.from("uname", resp.username())
                .sameSite("Lax").path("/").build();
        return ResponseEntity.ok()
                .header("Set-Cookie", uid.toString())
                .header("Set-Cookie", uname.toString())
                .body(resp);
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout() {
        // Clear cookies by setting them with maxAge=0
        ResponseCookie uid = ResponseCookie.from("uid", "")
                .sameSite("Lax").path("/").maxAge(0).build();
        ResponseCookie uname = ResponseCookie.from("uname", "")
                .sameSite("Lax").path("/").maxAge(0).build();
        return ResponseEntity.ok()
                .header("Set-Cookie", uid.toString())
                .header("Set-Cookie", uname.toString())
                .build();
    }

    @PostMapping("validate")
    public ResponseEntity<Void> validate(HttpServletRequest request) {
        if (authService.validateToken(request)) {
            logout();
        }

        return null;
    }

}
