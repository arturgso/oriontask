package br.com.oriontask.backend.config;

import br.com.oriontask.backend.shared.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisStartupValidator implements ApplicationRunner {

  private final RedisTokenService redisTokenService;

  @Value("${app.redis.fail-fast-on-startup:true}")
  private boolean failFastOnStartup;

  @Override
  public void run(ApplicationArguments args) {
    if (!failFastOnStartup) {
      log.info("Redis startup validation disabled by configuration");
      return;
    }

    try {
      boolean healthy = redisTokenService.ping();
      if (!healthy) {
        throw new IllegalStateException("Redis connection failed during startup");
      }
      log.info("Redis startup validation succeeded");
    } catch (Exception ex) {
      log.error("Fatal: unable to connect to Redis during startup", ex);
      throw new IllegalStateException("Redis connection failed during startup", ex);
    }
  }
}
