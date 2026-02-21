package br.com.oriontask.backend.service.user;

import br.com.oriontask.backend.exceptions.user.UserLookupExceptionImpl;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.UsersRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLookupServiceImpl implements UserLookupService {
  private final UsersRepository usersRepository;

  @Override
  public Users getRequiredUse(UUID userId) {
    return usersRepository.findById(userId).orElseThrow(UserLookupExceptionImpl::new);
  }
}
