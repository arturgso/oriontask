package br.com.oriontask.backend.shared.utils;

import br.com.oriontask.backend.users.model.Users;
import java.util.UUID;

public interface UserLookupService {
  Users getRequiredUse(UUID userId);
}
