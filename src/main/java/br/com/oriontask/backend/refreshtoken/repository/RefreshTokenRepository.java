package br.com.oriontask.backend.refreshtoken.repository;

import br.com.oriontask.backend.refreshtoken.models.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByUserId(UUID userId);
}
