package br.com.oriontask.backend.shared.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisTokenServiceTest {

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private RedisTokenService redisTokenService;

  @Test
  @DisplayName("Should return true when Redis ping responds PONG")
  void ping_shouldReturnTrueWhenPong() {
    when(redisTemplate.execute(org.mockito.ArgumentMatchers.<RedisCallback<String>>any()))
        .thenReturn("PONG");

    boolean result = redisTokenService.ping();

    assertTrue(result);
    verify(redisTemplate, times(1))
        .execute(org.mockito.ArgumentMatchers.<RedisCallback<String>>any());
  }

  @Test
  @DisplayName("Should return false when Redis ping response is unexpected")
  void ping_shouldReturnFalseWhenUnexpectedResponse() {
    when(redisTemplate.execute(org.mockito.ArgumentMatchers.<RedisCallback<String>>any()))
        .thenReturn("ERR");

    boolean result = redisTokenService.ping();

    assertFalse(result);
    verify(redisTemplate, times(1))
        .execute(org.mockito.ArgumentMatchers.<RedisCallback<String>>any());
  }

  @Test
  @DisplayName("Should store token and return it")
  void storeToken_shouldStoreAndReturnToken() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    UUID userId = UUID.randomUUID();
    String storedToken = redisTokenService.storeToken(userId);

    assertFalse(storedToken.isEmpty());
    verify(valueOperations, times(1))
        .set(
            argThat(key -> key.startsWith("password_reset:")),
            eq(userId.toString()),
            eq(Duration.ofHours(2)));
  }

  @Test
  @DisplayName("Should retrieve userId from token")
  void getUserIdFromToken_shouldRetrieveUserId() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    UUID userId = UUID.randomUUID();
    String token = "test-token";
    String key = "password_reset:" + token;

    when(valueOperations.get(key)).thenReturn(userId.toString());

    Optional<UUID> result = redisTokenService.getUserIdFromToken(token);

    assertTrue(result.isPresent());
    assertEquals(userId, result.get());
    verify(valueOperations, times(1)).get(key);
  }

  @Test
  @DisplayName("Should return empty when token not found")
  void getUserIdFromToken_shouldReturnEmptyWhenTokenNotFound() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    String token = "non-existent-token";
    String key = "password_reset:" + token;

    when(valueOperations.get(key)).thenReturn(null);

    Optional<UUID> result = redisTokenService.getUserIdFromToken(token);

    assertFalse(result.isPresent());
    verify(valueOperations, times(1)).get(key);
  }

  @Test
  @DisplayName("Should invalidate token")
  void invalidateToken_shouldDeleteToken() {
    String token = "token-to-invalidate";
    String key = "password_reset:" + token;

    redisTokenService.invalidateToken(token);

    verify(redisTemplate, times(1)).delete(key);
  }
}
