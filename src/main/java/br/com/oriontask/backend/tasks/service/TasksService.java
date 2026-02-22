package br.com.oriontask.backend.tasks.service;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.shared.utils.DharmaLookupService;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TasksService {

  private final TasksRepository repository;
  private final DharmaLookupService dharmaLookup;
  private final TasksMapper tasksMapper;

  private final TaskStatusTransitionPolicy statusPolicy;

  public TaskDTO create(NewTaskDTO createDTO, UUID userId) {
    Long dharmasId = createDTO.dharmasId();

    log.info("TasksService.create requested dharmasId={}", dharmasId);
    Dharmas dharmas = dharmaLookup.getRequiredDharma(dharmasId, userId);

    Tasks task = tasksMapper.toEntity(createDTO);
    task.setDharmas(dharmas);
    task.setUser(dharmas.getUser());
    task.setHidden(dharmas.getHidden());
    Long currentNowCount = getCurrentTasksCount(userId);
    task.setStatus(currentNowCount < 5 ? TaskStatus.NOW : TaskStatus.WAITING);

    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.create completed taskId={} dharmasId={}", result.id(), dharmasId);
    return result;
  }

  @Transactional
  public TaskDTO updateTask(UpdateTaskDTO editDTO, Long taskId, UUID userId) {
    log.info("TasksService.updateTask requested taskId={}", taskId);
    Tasks task = getTaskById(taskId, userId);
    statusPolicy.ensureStatusChangeAllowed(task);

    task = tasksMapper.partialUpdate(editDTO, task);
    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.updateTask completed taskId={}", taskId);
    return result;
  }

  @Transactional
  public TaskDTO moveToNow(Long taskId, UUID userId) {
    log.info("TasksService.moveToNow requested taskId={}", taskId);
    Tasks task = getTaskById(taskId, userId);

    statusPolicy.ensureStatusChangeAllowed(task);
    Long currentCount = getCurrentTasksCount(userId);

    statusPolicy.ensureNowLimitNotExceeded(currentCount, null);
    statusPolicy.markAsNow(task);

    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.moveToNow completed taskId={}", taskId);
    return result;
  }

  @Transactional
  @SuppressWarnings("deprecation")
  public TaskDTO changeStatus(Long taskId, TaskStatus newStatus, UUID userId) {
    log.info("TasksService.changeStatus requested taskId={} newStatus={}", taskId, newStatus);
    Tasks task = getTaskById(taskId, userId);

    statusPolicy.ensureStatusChangeAllowed(task);
    TaskStatus normalizedStatus = newStatus == TaskStatus.NEXT ? TaskStatus.WAITING : newStatus;

    Long currentCount = getCurrentTasksCount(task.getDharmas().getUser().getId());

    statusPolicy.ensureNowLimitNotExceeded(currentCount, normalizedStatus);
    statusPolicy.applyStatusTransition(task, normalizedStatus);

    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info(
        "TasksService.changeStatus completed taskId={} requestedStatus={} appliedStatus={}",
        taskId,
        newStatus,
        normalizedStatus);
    return result;
  }

  @Transactional
  public TaskDTO snoozeTask(Long taskId, UUID userId) {
    log.info("TasksService.snoozeTask requested taskId={}", taskId);
    Tasks task = getTaskById(taskId, userId);

    statusPolicy.ensureStatusChangeAllowed(task);
    statusPolicy.snoozeTask(task);

    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.snoozeTask completed taskId={}", taskId);
    return result;
  }

  @Transactional
  public TaskDTO markAsDone(Long taskId, UUID userId) {
    log.info("TasksService.markAsDone requested taskId={}", taskId);
    Tasks task = getTaskById(taskId, userId);

    statusPolicy.markAsDone(task);
    TaskDTO result = tasksMapper.toDTO(repository.save(task));
    log.info("TasksService.markAsDone completed taskId={}", taskId);
    return result;
  }

  public Page<TaskDTO> listTasks(
      UUID userId, Long dharmasId, TaskStatus status, Pageable pageable) {
    log.debug(
        "TasksService.listTasks requested userId={} dharmasId={} status={} page={} size={}",
        userId,
        dharmasId,
        status,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<Tasks> page;

    if (dharmasId != null && status != null) {
      page = repository.findByUserIdAndDharmasIdAndStatus(userId, dharmasId, status, pageable);
    } else if (dharmasId != null) {
      page = repository.findByUserIdAndDharmasId(userId, dharmasId, pageable);
    } else if (status != null) {
      page = repository.findByUserIdAndStatus(userId, status, pageable);
    } else {
      page = repository.findByUserId(userId, pageable);
    }

    Page<TaskDTO> result = page.map(tasksMapper::toDTO);
    log.debug(
        "TasksService.listTasks completed userId={} dharmasId={} status={} returned={}",
        userId,
        dharmasId,
        status,
        result.getNumberOfElements());
    return result;
  }

  @Transactional
  public void deleteTask(Long taskId, UUID userId) {
    log.info("TasksService.deleteTask requested taskId={}", taskId);
    Tasks task = getTaskById(taskId, userId);

    statusPolicy.ensureStatusChangeAllowed(task, true);

    repository.delete(task);
    log.info("TasksService.deleteTask completed taskId={}", taskId);
  }

  @Transactional
  @Scheduled(fixedDelayString = "${task.waiting-promotion.interval-ms:600000}")
  public void promoteRecentWaitingTasksToNow() {
    repository
        .findDistinctUserIdsByStatus(TaskStatus.WAITING)
        .forEach(
            userId -> {
              Long currentCount = getCurrentTasksCount(userId);
              if (currentCount >= 5) {
                return;
              }

              repository
                  .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, TaskStatus.WAITING)
                  .ifPresent(
                      waitingTask -> {
                        statusPolicy.markAsNow(waitingTask);
                        repository.save(waitingTask);
                        log.info(
                            "TasksService.promoteRecentWaitingTasksToNow promoted taskId={} userId={}",
                            waitingTask.getId(),
                            userId);
                      });
            });
  }

  private Long getCurrentTasksCount(UUID userId) {
    return repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW);
  }

  private Tasks getTaskById(Long taskId, UUID userId) {
    return repository
        .findByIdAndUserId(taskId, userId)
        .orElseThrow(
            () -> {
              log.warn("TasksService.getTaskById task not found taskId={}", taskId);
              return new IllegalArgumentException("Task not found");
            });
  }
}
