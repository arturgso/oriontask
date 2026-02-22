package br.com.oriontask.backend.users.repository;

import br.com.oriontask.backend.users.model.Users;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, UUID> {
  Optional<Users> findByEmail(String email);

  Optional<Users> findByEmailIgnoreCase(String email);

  Optional<Users> findByConfirmationToken(String token);
}
