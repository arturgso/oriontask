package br.com.oriontask.backend.tasks.service;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.dharmas.repository.DharmasRepository;
import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.tasks.dto.NewTaskDTO;
import br.com.oriontask.backend.tasks.dto.TaskDTO;
import br.com.oriontask.backend.tasks.dto.UpdateTaskDTO;
import br.com.oriontask.backend.tasks.mapper.TasksMapper;
import br.com.oriontask.backend.tasks.model.Tasks;
import br.com.oriontask.backend.tasks.policy.TaskStatusTransitionPolicy;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TasksService {

  private final TasksRepository repository;
  private final DharmasRepository dharmasRepository;
  private final TasksMapper tasksMapper;

  private final TaskStatusTransitionPolicy statusPolicy;

  public TaskDTO create(NewTaskDTO createDTO, Long dharmasId) {
    log.info("TasksService.create requested dharmasId={}", dharmasId);
    Dharmas dharmas =
        dharmasRepository
            .findById(dharmasId)
            .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

    Tasks task = tasksMapper.toEntity(createDTO);
    task.setDharmas(dharmas);
    task.setHidden(dharmas.getHidden());
    task.setStatus(TaskStatus.NEXT);

    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.create completed taskId={} dharmasId={}", result.id(), dharmasId);
    return result;
  }

  @Transactional
  public TaskDTO updateTask(UpdateTaskDTO editDTO, Long taskId) {
    log.info("TasksService.updateTask requested taskId={}", taskId);
    Tasks task = getTaskById(taskId);
    statusPolicy.ensureStatusChangeAllowed(task);

    task = tasksMapper.partialUpdate(editDTO, task);
    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.updateTask completed taskId={}", taskId);
    return result;
  }

  @Transactional
  public TaskDTO moveToNow(Long taskId) {
    log.info("TasksService.moveToNow requested taskId={}", taskId);
    Tasks task = getTaskById(taskId);

    statusPolicy.ensureStatusChangeAllowed(task);
    Long currentCount = getCurrentTasksCount(task.getDharmas().getUser().getId());

    statusPolicy.ensureNowLimitNotExceeded(currentCount, null);
    statusPolicy.markAsNow(task);

    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.moveToNow completed taskId={}", taskId);
    return result;
  }

  @Transactional
  public TaskDTO changeStatus(Long taskId, TaskStatus newStatus) {
    log.info("TasksService.changeStatus requested taskId={} newStatus={}", taskId, newStatus);
    Tasks task = getTaskById(taskId);

    statusPolicy.ensureStatusChangeAllowed(task);

    Long currentCount = getCurrentTasksCount(task.getDharmas().getUser().getId());

    statusPolicy.ensureNowLimitNotExceeded(currentCount, newStatus);

    if (newStatus == TaskStatus.SNOOZED) {
      statusPolicy.snoozeTask(task);
    } else {
      statusPolicy.applyStatusTransition(task, newStatus);
    }

    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.changeStatus completed taskId={} newStatus={}", taskId, newStatus);
    return result;
  }

  @Transactional
  public TaskDTO markAsDone(Long taskId) {
    log.info("TasksService.markAsDone requested taskId={}", taskId);
    Tasks task = getTaskById(taskId);

    statusPolicy.markAsDone(task);
    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.markAsDone completed taskId={}", taskId);
    return result;
  }

  public Page<TaskDTO> getTasksByDharmas(Long dharmasId, Pageable pageable) {
    log.debug(
        "TasksService.getTasksByDharmas requested dharmasId={} page={} size={}",
        dharmasId,
        pageable.getPageNumber(),
        pageable.getPageSize());
    Page<TaskDTO> result = repository.findByDharmasId(dharmasId, pageable).map(tasksMapper::toDTO);
    log.debug(
        "TasksService.getTasksByDharmas completed dharmasId={} returned={}",
        dharmasId,
        result.getNumberOfElements());
    return result;
  }

  public Page<TaskDTO> getTasksByDharmasAndStatus(
      Long dharmasId, TaskStatus status, Pageable pageable) {
    log.debug(
        "TasksService.getTasksByDharmasAndStatus requested dharmasId={} status={} page={} size={}",
        dharmasId,
        status,
        pageable.getPageNumber(),
        pageable.getPageSize());
    Page<TaskDTO> result =
        repository.findByDharmasIdAndStatus(dharmasId, status, pageable).map(tasksMapper::toDTO);
    log.debug(
        "TasksService.getTasksByDharmasAndStatus completed dharmasId={} status={} returned={}",
        dharmasId,
        status,
        result.getNumberOfElements());
    return result;
  }

  public Page<TaskDTO> getTasksByUserAndStatus(
      String userId, TaskStatus status, Pageable pageable) {
    log.debug(
        "TasksService.getTasksByUserAndStatus requested userId={} status={} page={} size={}",
        userId,
        status,
        pageable.getPageNumber(),
        pageable.getPageSize());
    Page<TaskDTO> result =
        repository
            .findByDharmasUserIdAndStatus(UUID.fromString(userId), status, pageable)
            .map(tasksMapper::toDTO);
    log.debug(
        "TasksService.getTasksByUserAndStatus completed userId={} status={} returned={}",
        userId,
        status,
        result.getNumberOfElements());
    return result;
  }

  public Page<TaskDTO> getTasksByUser(String userId, Pageable pageable) {
    log.debug(
        "TasksService.getTasksByUser requested userId={} page={} size={}",
        userId,
        pageable.getPageNumber(),
        pageable.getPageSize());
    Page<TaskDTO> result =
        repository.findByDharmasUserId(UUID.fromString(userId), pageable).map(tasksMapper::toDTO);
    log.debug(
        "TasksService.getTasksByUser completed userId={} returned={}",
        userId,
        result.getNumberOfElements());
    return result;
  }

  @Transactional
  public void deleteTask(Long taskId) {
    log.info("TasksService.deleteTask requested taskId={}", taskId);
    Tasks task = getTaskById(taskId);

    statusPolicy.ensureStatusChangeAllowed(task, true);

    repository.delete(task);
    log.info("TasksService.deleteTask completed taskId={}", taskId);
  }

  private Long getCurrentTasksCount(UUID userId) {
    return repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW);
  }

  private Tasks getTaskById(Long taskId) {
    return repository
        .findById(taskId)
        .orElseThrow(
            () -> {
              log.warn("TasksService.getTaskById task not found taskId={}", taskId);
              return new IllegalArgumentException("Task not found");
            });
  }
}
