package br.com.oriontask.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.model.Tasks;

public interface TasksRepository extends JpaRepository<Tasks, Long> {
    Optional<Tasks> findById(Long id);

    List<Tasks> findByDharmaId(Long dharmaId);

    Page<Tasks> findByDharmaId(Long dharmaId, Pageable pageable);

    Page<Tasks> findByDharmaIdAndStatus(Long dharmaId, TaskStatus status, Pageable pageable);

    Page<Tasks> findByDharmaUserIdAndStatus(UUID userId, TaskStatus status, Pageable pageable);

    Long countByDharmaId(Long dharmaId);

    Long countByDharmaUserIdAndStatus(UUID userId, TaskStatus status);
}