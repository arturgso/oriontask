package br.com.oriontask.backend.repository;

import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.model.Tasks;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TasksRepository extends JpaRepository<Tasks, Long> {
  Optional<Tasks> findById(Long id);

  List<Tasks> findByDharmasId(Long dharmasId);

  Page<Tasks> findByDharmasId(Long dharmasId, Pageable pageable);

  Page<Tasks> findByDharmasIdAndStatus(Long dharmasId, TaskStatus status, Pageable pageable);

  @Query(
      "SELECT t FROM Tasks t WHERE t.dharmas.user.id = :userId AND "
          + "((t.status = :status AND :status = br.com.oriontask.backend.enums.TaskStatus.NOW) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.enums.TaskStatus.NOW AND t.snoozedUntil <= CURRENT_TIMESTAMP) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.enums.TaskStatus.NEXT AND (t.snoozedUntil IS NULL OR t.snoozedUntil <= CURRENT_TIMESTAMP)) OR "
          + "(t.status = :status AND :status NOT IN (br.com.oriontask.backend.enums.TaskStatus.NOW, br.com.oriontask.backend.enums.TaskStatus.NEXT)))")
  Page<Tasks> findByDharmasUserIdAndStatus(
      java.util.UUID userId, TaskStatus status, Pageable pageable);

  Long countByDharmasId(Long dharmasId);

  @Query(
      "SELECT COUNT(t) FROM Tasks t WHERE t.dharmas.user.id = :userId AND "
          + "((t.status = :status AND :status = br.com.oriontask.backend.enums.TaskStatus.NOW) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.enums.TaskStatus.NOW AND t.snoozedUntil <= CURRENT_TIMESTAMP) OR "
          + "(t.status = 'NEXT' AND :status = br.com.oriontask.backend.enums.TaskStatus.NEXT AND (t.snoozedUntil IS NULL OR t.snoozedUntil <= CURRENT_TIMESTAMP)) OR "
          + "(t.status = :status AND :status NOT IN (br.com.oriontask.backend.enums.TaskStatus.NOW, br.com.oriontask.backend.enums.TaskStatus.NEXT)))")
  Long countByDharmasUserIdAndStatus(java.util.UUID userId, TaskStatus status);
}
