package br.com.oriontask.backend.shared.utils;

import br.com.oriontask.backend.users.model.Users;
import java.util.UUID;

public interface UserLookupService {
  Users getRequiredUser(UUID userId);

  Users getByEmail(String email);

  boolean existsByEmail(String email);
}
