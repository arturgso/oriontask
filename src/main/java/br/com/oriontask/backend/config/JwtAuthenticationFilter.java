package br.com.oriontask.backend.config;

import br.com.oriontask.backend.auth.service.TokenService;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final RedisTemplate<Object, Object> redisTemplate;

  private static final String BLACKLIST_TOKEN_PREFIX = "blacklisted_token:";

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = tokenService.extractTokenFromRequest(request);
    if (token != null) {

      try {
        DecodedJWT decodedJWT = tokenService.validateAccessToken(token);

        String jti = decodedJWT.getId();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_TOKEN_PREFIX + jti))) {
          throw new RuntimeException("Token invalidated");
        }

        UUID userId = UUID.fromString(decodedJWT.getSubject());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Authenticated userId={}", userId);

      } catch (Exception e) {
        log.warn("JWT validation failed: {}", e.getMessage());
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }
}
