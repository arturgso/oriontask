package br.com.oriontask.backend.tasks.repository;

import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.tasks.model.Tasks;
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

  @Query(
      "SELECT t FROM Tasks t WHERE t.user.id = :userId AND "
          + "((t.status = :status AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NOW) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NOW AND t.snoozedUntil <= CURRENT_TIMESTAMP) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NEXT AND (t.snoozedUntil IS NULL OR t.snoozedUntil <= CURRENT_TIMESTAMP)) OR "
          + "(t.status = :status AND :status NOT IN (br.com.oriontask.backend.shared.enums.TaskStatus.NOW, br.com.oriontask.backend.shared.enums.TaskStatus.NEXT)))")
  Page<Tasks> findByUserIdAndStatus(UUID userId, TaskStatus status, Pageable pageable);

  @Query(
      "SELECT t FROM Tasks t WHERE t.user.id = :userId AND t.dharmas.id = :dharmasId AND "
          + "((t.status = :status AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NOW) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NOW AND t.snoozedUntil <= CURRENT_TIMESTAMP) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NEXT AND (t.snoozedUntil IS NULL OR t.snoozedUntil <= CURRENT_TIMESTAMP)) OR "
          + "(t.status = :status AND :status NOT IN (br.com.oriontask.backend.shared.enums.TaskStatus.NOW, br.com.oriontask.backend.shared.enums.TaskStatus.NEXT)))")
  Page<Tasks> findByUserIdAndDharmasIdAndStatus(
      UUID userId, Long dharmasId, TaskStatus status, Pageable pageable);

  Long countByDharmasId(Long dharmasId);

  @Query(
      "SELECT COUNT(t) FROM Tasks t WHERE t.user.id = :userId AND "
          + "((t.status = :status AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NOW) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NOW AND t.snoozedUntil <= CURRENT_TIMESTAMP) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.shared.enums.TaskStatus.NEXT AND (t.snoozedUntil IS NULL OR t.snoozedUntil <= CURRENT_TIMESTAMP)) OR "
          + "(t.status = :status AND :status NOT IN (br.com.oriontask.backend.shared.enums.TaskStatus.NOW, br.com.oriontask.backend.shared.enums.TaskStatus.NEXT)))")
  Long countByDharmasUserIdAndStatus(java.util.UUID userId, TaskStatus status);
}
