package br.com.oriontask.backend.service.user;

import br.com.oriontask.backend.exceptions.user.UserLookupExceptionImpl;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.UsersRepository;
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
  public Users getRequiredUse(UUID userId) {
    log.debug("UserLookupService.getRequiredUse requested userId={}", userId);
    return usersRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.warn("UserLookupService.getRequiredUse user not found userId={}", userId);
              return new UserLookupExceptionImpl();
            });
  }
}
