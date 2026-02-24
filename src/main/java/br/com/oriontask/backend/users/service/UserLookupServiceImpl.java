package br.com.oriontask.backend.users.service;

import br.com.oriontask.backend.shared.utils.UserLookupService;
import br.com.oriontask.backend.users.exception.UserLookupExceptionImpl;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLookupServiceImpl implements UserLookupService {
  private final UsersRepository usersRepository;

  @Override
  public Users getRequiredUser(UUID userId) {
    log.debug("UserLookupService.getRequiredUser requested userId={}", userId);
    return usersRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.warn("UserLookupService.getRequiredUser user not found userId={}", userId);
              return new UserLookupExceptionImpl();
            });
  }

  @Override
  public Users getByEmail(String email) {
    log.debug("UserLookupService.getByEmail requested email={}", email);
    return usersRepository
        .findByEmail(email)
        .orElseThrow(
            () -> {
              log.warn("UserLookupService.getByEmail user not found email={}", email);
              return new UserLookupExceptionImpl();
            });
  }

  @Override
  public boolean existsByEmail(String email) {
    log.debug("UserLookupService.existsByEmail requested email={}", email);
    boolean exists = usersRepository.existsByEmail(email);
    if (!exists) {
      log.warn("UserLookupService.existsByEmail user not found email={}", email);
    }
    return exists;
  }
}
