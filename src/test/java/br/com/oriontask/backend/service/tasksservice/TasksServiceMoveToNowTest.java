package br.com.oriontask.backend.service.tasksservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.shared.utils.DharmaLookupService;
import br.com.oriontask.backend.tasks.dto.TaskDTO;
import br.com.oriontask.backend.tasks.exception.NowTasksLimitExceededException;
import br.com.oriontask.backend.tasks.exception.TaskStatusChangeNotAllowedException;
import br.com.oriontask.backend.tasks.mapper.TasksMapper;
import br.com.oriontask.backend.tasks.model.Tasks;
import br.com.oriontask.backend.tasks.policy.TaskStatusTransitionPolicy;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import br.com.oriontask.backend.tasks.service.TasksService;
import br.com.oriontask.backend.users.model.Users;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TasksServiceMoveToNowTest {

  @Mock private TasksRepository repository;
  @Mock private DharmaLookupService dharmaLookup;
  @Mock private TasksMapper tasksMapper;
  @Mock private TaskStatusTransitionPolicy statusPolicy;

  @InjectMocks private TasksService tasksService;

  @Test
  @DisplayName("Should throw when task is not found")
  void moveToNowShouldThrowWhenTaskNotFound() {
    UUID userId = UUID.randomUUID();
    when(repository.findByIdAndUserId(99L, userId)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> tasksService.moveToNow(99L, userId));

    assertEquals("Task not found", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
  }

  @Test
  @DisplayName("Should block move to NOW when task status change is not allowed")
  void moveToNowShouldThrowWhenTaskCannotChangeStatus() {
    Tasks task = buildTask(10L, TaskStatus.DONE);
    UUID userId = task.getDharmas().getUser().getId();
    when(repository.findByIdAndUserId(10L, userId)).thenReturn(Optional.of(task));
    doThrow(new TaskStatusChangeNotAllowedException())
        .when(statusPolicy)
        .ensureStatusChangeAllowed(task);

    TaskStatusChangeNotAllowedException exception =
        assertThrows(
            TaskStatusChangeNotAllowedException.class, () -> tasksService.moveToNow(10L, userId));

    assertEquals("Completed tasks cannot change status", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
  }

  @Test
  @DisplayName("Should block move to NOW when NOW limit is reached")
  void moveToNowShouldThrowWhenNowLimitReached() {
    Tasks task = buildTask(11L, TaskStatus.WAITING);
    UUID userId = task.getDharmas().getUser().getId();

    when(repository.findByIdAndUserId(11L, userId)).thenReturn(Optional.of(task));
    when(repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW)).thenReturn(5L);
    doThrow(new NowTasksLimitExceededException())
        .when(statusPolicy)
        .ensureNowLimitNotExceeded(5L, null);

    NowTasksLimitExceededException exception =
        assertThrows(
            NowTasksLimitExceededException.class, () -> tasksService.moveToNow(11L, userId));

    assertEquals("Maximum of 5 tasks in NOW reached", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
  }

  @Test
  @DisplayName("Should move task to NOW and clear snooze")
  void moveToNowShouldMoveTaskSuccessfully() {
    Tasks task = buildTask(12L, TaskStatus.SNOOZED);
    task.setSnoozedUntil(new Timestamp(System.currentTimeMillis() + 3600000));
    UUID userId = task.getDharmas().getUser().getId();

    when(repository.findByIdAndUserId(12L, userId)).thenReturn(Optional.of(task));
    when(repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW)).thenReturn(2L);
    doAnswer(
            invocation -> {
              Tasks target = invocation.getArgument(0);
              target.setStatus(TaskStatus.NOW);
              target.setSnoozedUntil(null);
              return null;
            })
        .when(statusPolicy)
        .markAsNow(task);
    when(repository.save(task)).thenReturn(task);
    when(tasksMapper.toDTO(task)).thenAnswer(invocation -> toDTO(invocation.getArgument(0)));

    TaskDTO result = tasksService.moveToNow(12L, userId);

    assertNotNull(result);
    assertEquals(TaskStatus.NOW, result.status());
    assertNull(result.snoozedUntil());

    verify(statusPolicy).ensureStatusChangeAllowed(task);
    verify(statusPolicy).ensureNowLimitNotExceeded(2L, null);
    verify(statusPolicy).markAsNow(task);
    verify(repository).save(task);
  }

  private Tasks buildTask(Long taskId, TaskStatus status) {
    Users user = Users.builder().id(UUID.randomUUID()).build();
    Dharmas dharmas = Dharmas.builder().id(100L).user(user).build();

    return Tasks.builder()
        .id(taskId)
        .dharmas(dharmas)
        .title("Task title")
        .description("Task description")
        .status(status)
        .hidden(false)
        .createdAt(new Timestamp(System.currentTimeMillis() - 10000))
        .updatedAt(new Timestamp(System.currentTimeMillis() - 10000))
        .build();
  }

  private TaskDTO toDTO(Tasks task) {
    return new TaskDTO(
        task.getId(),
        task.getDharmas().getId(),
        task.getTitle(),
        task.getDescription(),
        task.getKarmaType(),
        task.getEffortLevel(),
        task.getStatus(),
        task.getHidden(),
        task.getCompletedAt(),
        task.getSnoozedUntil(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }
}
