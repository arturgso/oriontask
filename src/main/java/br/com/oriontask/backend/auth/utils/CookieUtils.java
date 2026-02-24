package br.com.oriontask.backend.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

  private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(7);

  public static String getRefreshToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(REFRESH_TOKEN_COOKIE_NAME)) {
        return cookie.getValue();
      }
    }

    return null;
  }

  public static ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(REFRESH_TOKEN_MAX_AGE)
        .sameSite("Lax")
        .build();
  }

  public static ResponseCookie clearRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();
  }
}
