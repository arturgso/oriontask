package br.com.oriontask.backend.dharmas.repository;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.users.model.Users;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DharmasRepository extends JpaRepository<Dharmas, Long> {
  Optional<Dharmas> findById(Long id);

  Long countByUser(Users user);

  List<Dharmas> findByUserId(UUID userId);

  List<Dharmas> findByUserIdAndHiddenFalse(UUID userId);

  Optional<Dharmas> findByIdAndUserId(Long dharmasId, UUID userId);
}
