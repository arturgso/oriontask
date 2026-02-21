package br.com.oriontask.backend.service.user;

import br.com.oriontask.backend.model.Users;
import java.util.UUID;

public interface UserLookupService {
  Users getRequiredUse(UUID userId);
}
