package br.com.oriontask.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.oriontask.backend.model.Dharma;
import br.com.oriontask.backend.model.Users;

public interface DharmaRepository extends JpaRepository<Dharma, Long>{
    Optional<Dharma> findById(Long id);
    Long countByUser(Users user);
    Optional<Dharma> findByUser(UUID userId);
}
