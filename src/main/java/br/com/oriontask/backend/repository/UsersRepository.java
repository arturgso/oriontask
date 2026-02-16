package br.com.oriontask.backend.repository;

import br.com.oriontask.backend.model.Users;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, UUID> {
  Optional<Users> findByUsername(String username);

  Optional<Users> findByEmail(String email);
}
