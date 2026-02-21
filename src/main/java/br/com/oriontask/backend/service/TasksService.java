package br.com.oriontask.backend.service;

import br.com.oriontask.backend.dto.tasks.NewTaskDTO;
import br.com.oriontask.backend.dto.tasks.TaskDTO;
import br.com.oriontask.backend.dto.tasks.UpdateTaskDTO;
import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.mappers.TasksMapper;
import br.com.oriontask.backend.model.Dharmas;
import br.com.oriontask.backend.model.Tasks;
import br.com.oriontask.backend.policy.TaskStatusTransitionPolicy;
import br.com.oriontask.backend.repository.DharmasRepository;
import br.com.oriontask.backend.repository.TasksRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TasksService {

  private final TasksRepository repository;
  private final DharmasRepository dharmasRepository;
  private final TasksMapper tasksMapper;

  private final TaskStatusTransitionPolicy statusPolicy;

  private static final int MAX_NOW_TASKS = 5;

  public TaskDTO create(NewTaskDTO createDTO, Long dharmasId) {
    Dharmas dharmas =
        dharmasRepository
            .findById(dharmasId)
            .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

    Tasks task = tasksMapper.toEntity(createDTO);
    task.setDharmas(dharmas);
    task.setHidden(dharmas.getHidden());
    task.setStatus(TaskStatus.NEXT);

    return tasksMapper.toDTO(repository.save(task));
  }

  @Transactional
  public TaskDTO updateTask(UpdateTaskDTO editDTO, Long taskId) {
    Tasks task =
        repository
            .findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    statusPolicy.ensureStatusChangeAllowed(task);

    task = tasksMapper.partialUpdate(editDTO, task);

    return tasksMapper.toDTO(repository.save(task));
  }

  @Transactional
  public TaskDTO moveToNow(Long taskId) {
    Tasks task =
        repository
            .findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    statusPolicy.ensureStatusChangeAllowed(task);
    Long currentCount =
        repository.countByDharmasUserIdAndStatus(
            task.getDharmas().getUser().getId(), TaskStatus.NOW);

    statusPolicy.ensureNowLimitNotExceeded(currentCount, null);
    statusPolicy.markAsNow(task);

    return tasksMapper.toDTO(repository.save(task));
  }

  @Transactional
  public TaskDTO changeStatus(Long taskId, TaskStatus newStatus) {
    Tasks task =
        repository
            .findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    statusPolicy.ensureStatusChangeAllowed(task);

    Long currentTasksCount =
        repository.countByDharmasUserIdAndStatus(
            task.getDharmas().getUser().getId(), TaskStatus.NOW);
    statusPolicy.ensureNowLimitNotExceeded(currentTasksCount, newStatus);

    if (newStatus == TaskStatus.SNOOZED) {
      statusPolicy.snoozeTask(task);
    } else {
      statusPolicy.applyStatusTransition(task, newStatus);
    }

    return tasksMapper.toDTO(repository.save(task));
  }

  @Transactional
  public TaskDTO markAsDone(Long taskId) {
    Tasks task =
        repository
            .findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    statusPolicy.markAsDone(task);
    return tasksMapper.toDTO(repository.save(task));
  }

  public Page<TaskDTO> getTasksByDharmas(Long dharmasId, Pageable pageable) {
    return repository.findByDharmasId(dharmasId, pageable).map(tasksMapper::toDTO);
  }

  public Page<TaskDTO> getTasksByDharmasAndStatus(
      Long dharmasId, TaskStatus status, Pageable pageable) {
    return repository.findByDharmasIdAndStatus(dharmasId, status, pageable).map(tasksMapper::toDTO);
  }

  public Page<TaskDTO> getTasksByUserAndStatus(
      String userId, TaskStatus status, Pageable pageable) {
    return repository
        .findByDharmasUserIdAndStatus(UUID.fromString(userId), status, pageable)
        .map(tasksMapper::toDTO);
  }

  public Page<TaskDTO> getTasksByUser(String userId, Pageable pageable) {
    return repository
        .findByDharmasUserId(UUID.fromString(userId), pageable)
        .map(tasksMapper::toDTO);
  }

  @Transactional
  public void deleteTask(Long taskId) {
    Tasks task =
        repository
            .findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    if (task.getStatus() == TaskStatus.DONE) {
      throw new IllegalStateException("Completed tasks cannot be deleted (history)");
    }

    repository.delete(task);
  }
}
