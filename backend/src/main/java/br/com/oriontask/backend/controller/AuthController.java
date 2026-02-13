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
    public ResponseEntity<AuthResponseDTO> signup(@RequestBody @Validated SignupRequestDTO req,
                                                  HttpServletRequest request) {
        AuthResponseDTO resp = authService.signup(req);
        // Set minimal cookies with id and username
        ResponseCookie uid = buildCookie("uid", resp.id().toString(), request, false);
        ResponseCookie uname = buildCookie("uname", resp.username(), request, false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie", uid.toString())
                .header("Set-Cookie", uname.toString())
                .body(resp);
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Validated LoginRequestDTO req,
                                                 HttpServletRequest request) {
        AuthResponseDTO resp = authService.login(req);
        ResponseCookie uid = buildCookie("uid", resp.id().toString(), request, false);
        ResponseCookie uname = buildCookie("uname", resp.username(), request, false);
        return ResponseEntity.ok()
                .header("Set-Cookie", uid.toString())
                .header("Set-Cookie", uname.toString())
                .body(resp);
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // Clear cookies by setting them with maxAge=0
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
            return ResponseEntity.ok().body(null);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    private static ResponseCookie buildCookie(String name, String value, HttpServletRequest request, boolean clear) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .sameSite("None")
                .path("/")
                .secure(true);
        if (clear) {
            builder.maxAge(0);
        }
        return builder.build();
    }

}
