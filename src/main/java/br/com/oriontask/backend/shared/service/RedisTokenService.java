package br.com.oriontask.backend.shared.service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenService {

  private static final String PASSWORD_RESET_PREFIX = "password_reset:";
  private static final Duration TOKEN_VALIDITY = Duration.ofHours(2);

  private final StringRedisTemplate redisTemplate;

  public boolean ping() {
    String response =
        redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
    boolean healthy = "PONG".equalsIgnoreCase(response);
    if (healthy) {
      log.info("Redis ping successful");
    } else {
      log.warn("Redis ping returned unexpected response={}", response);
    }
    return healthy;
  }

  public String storeToken(UUID userId) {
    String token = UUID.randomUUID().toString();
    String key = PASSWORD_RESET_PREFIX + token;
    redisTemplate.opsForValue().set(key, userId.toString(), TOKEN_VALIDITY);
    log.info("Stored password reset token for userId={} with key={}", userId, key);
    return token;
  }

  public Optional<UUID> getUserIdFromToken(String token) {
    String key = PASSWORD_RESET_PREFIX + token;
    String userIdString = redisTemplate.opsForValue().get(key);
    if (userIdString != null) {
      log.info("Retrieved userId={} from password reset token key={}", userIdString, key);
      return Optional.of(UUID.fromString(userIdString));
    }
    log.warn("Password reset token key={} not found or expired", key);
    return Optional.empty();
  }

  public void invalidateToken(String token) {
    String key = PASSWORD_RESET_PREFIX + token;
    redisTemplate.delete(key);
    log.info("Invalidated password reset token key={}", key);
  }
}
