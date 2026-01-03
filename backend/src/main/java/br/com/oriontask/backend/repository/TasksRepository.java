package br.com.oriontask.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.model.Tasks;

public interface TasksRepository extends JpaRepository<Tasks, Long>{
    Optional<Tasks> findById(Long id);
    List<Tasks> findByDharmaId(Long dharmaId);
    List<Tasks> findByDharmaIdAndStatus(Long dharmaId, TaskStatus status);
    List<Tasks> findByDharmaUserIdAndStatus(UUID userId, TaskStatus status);
    Long countByDharmaId(Long dharmaId);
    Long countByDharmaUserIdAndStatus(UUID userId, TaskStatus status);
} 