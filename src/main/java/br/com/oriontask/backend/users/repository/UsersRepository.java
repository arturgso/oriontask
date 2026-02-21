package br.com.oriontask.backend.users.repository;

import br.com.oriontask.backend.users.model.Users;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, UUID> {
  Optional<Users> findByUsername(String username);

  Optional<Users> findByEmail(String email);

  Optional<Users> findByEmailIgnoreCaseOrUsername(String email, String username);
}
