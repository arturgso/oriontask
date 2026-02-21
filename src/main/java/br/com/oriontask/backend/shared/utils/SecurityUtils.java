package br.com.oriontask.backend.shared.utils;

import java.nio.file.AccessDeniedException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
  public void isOwner(UUID userId, Authentication authentication) throws AccessDeniedException {
    if (authentication == null || authentication.getName() == null) {
      throw new AccessDeniedException("Unauthenticated");
    }

    UUID tokenUserId;

    try {
      tokenUserId = UUID.fromString(authentication.getName());
    } catch (IllegalArgumentException e) {
      throw new AccessDeniedException("Invalid authentication principal");
    }

    if (!userId.equals(tokenUserId)) {
      throw new AccessDeniedException("Invalid authentication principal");
    }
  }
}
