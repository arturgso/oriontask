package br.com.oriontask.backend.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

  public static String getRefreshToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals("refresh_token")) {
        return cookie.getValue();
      }
    }

    return null;
  }
}
