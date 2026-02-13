package br.com.oriontask.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.oriontask.backend.model.Dharmas;
import br.com.oriontask.backend.model.Users;

public interface DharmasRepository extends JpaRepository<Dharmas, Long>{
    Optional<Dharmas> findById(Long id);
    Long countByUser(Users user);
    List<Dharmas> findByUserId(UUID userId);
    List<Dharmas> findByUserIdAndHiddenFalse(UUID userId);
}
