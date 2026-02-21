package br.com.oriontask.backend.tasks.repository;

import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.tasks.model.Tasks;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TasksRepository extends JpaRepository<Tasks, Long> {
  Optional<Tasks> findById(Long id);

  Optional<Tasks> findByIdAndUserId(Long id, UUID userId);

  Page<Tasks> findByDharmasId(Long dharmasId, Pageable pageable);

  Page<Tasks> findByUserId(UUID userId, Pageable pageable);

  Page<Tasks> findByUserIdAndDharmasId(UUID userId, Long dharmasId, Pageable pageable);

  Page<Tasks> findByUserIdAndStatus(UUID userId, TaskStatus status, Pageable pageable);

  Page<Tasks> findByUserIdAndDharmasIdAndStatus(
      UUID userId, Long dharmasId, TaskStatus status, Pageable pageable);

  Long countByDharmasId(Long dharmasId);

  Long countByDharmasUserIdAndStatus(java.util.UUID userId, TaskStatus status);

  @Query("SELECT DISTINCT t.user.id FROM Tasks t WHERE t.status = :status")
  List<UUID> findDistinctUserIdsByStatus(TaskStatus status);

  Optional<Tasks> findFirstByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, TaskStatus status);
}
