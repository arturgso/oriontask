package br.com.oriontask.backend.users.service;

import br.com.oriontask.backend.users.model.Users;
import java.util.UUID;

public interface UserLookupService {
  Users getRequiredUse(UUID userId);
}
