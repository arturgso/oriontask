package br.com.oriontask.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dto.tasks.TaskDTO;
import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.exceptions.task.NowTasksLimitExceededException;
import br.com.oriontask.backend.exceptions.task.TaskStatusChangeNotAllowedException;
import br.com.oriontask.backend.mappers.TasksMapper;
import br.com.oriontask.backend.model.Dharmas;
import br.com.oriontask.backend.model.Tasks;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.policy.TaskStatusTransitionPolicy;
import br.com.oriontask.backend.repository.DharmasRepository;
import br.com.oriontask.backend.repository.TasksRepository;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TasksServiceChangeStatusTest {

  @Mock private TasksRepository repository;

  @Mock private DharmasRepository dharmasRepository;

  @Mock private TasksMapper tasksMapper;

  @Spy private TaskStatusTransitionPolicy statusPolicy = new TaskStatusTransitionPolicy();

  @InjectMocks private TasksService tasksService;

  @Test
  @DisplayName("Should throw when task does not exist")
  void changeStatusShouldThrowWhenTaskNotFound() {
    when(repository.findById(10L)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> tasksService.changeStatus(10L, TaskStatus.NOW));

    assertEquals("Task not found", exception.getMessage());
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Should block status change for DONE task")
  void changeStatusShouldThrowWhenTaskIsDone() {
    Tasks task = buildTask(1L, TaskStatus.DONE);
    when(repository.findById(1L)).thenReturn(Optional.of(task));

    assertThrows(
        TaskStatusChangeNotAllowedException.class,
        () -> tasksService.changeStatus(1L, TaskStatus.NEXT));
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Should block move to NOW when limit is reached")
  void changeStatusShouldThrowWhenNowLimitReached() {
    Tasks task = buildTask(2L, TaskStatus.NEXT);
    UUID userId = task.getDharmas().getUser().getId();

    when(repository.findById(2L)).thenReturn(Optional.of(task));
    when(repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW)).thenReturn(5L);

    NowTasksLimitExceededException exception =
        assertThrows(
            NowTasksLimitExceededException.class,
            () -> tasksService.changeStatus(2L, TaskStatus.NOW));

    assertEquals("Maximum of 5 tasks in NOW reached", exception.getMessage());
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Should set SNOOZED with snoozedUntil and updatedAt")
  void changeStatusShouldSetSnoozedAndSnooze() {
    Tasks task = buildTask(3L, TaskStatus.NOW);
    Timestamp before = new Timestamp(System.currentTimeMillis());

    when(repository.findById(3L)).thenReturn(Optional.of(task));
    when(repository.countByDharmasUserIdAndStatus(
            eq(task.getDharmas().getUser().getId()), eq(TaskStatus.NOW)))
        .thenReturn(0L);
    when(repository.save(any(Tasks.class))).thenAnswer(invocation -> invocation.getArgument(0));
    stubMapperToDTO();

    TaskDTO result = tasksService.changeStatus(3L, TaskStatus.SNOOZED);
    Timestamp after = new Timestamp(System.currentTimeMillis());

    assertEquals(TaskStatus.SNOOZED, result.status());
    assertNotNull(result.snoozedUntil());
    assertNotNull(result.updatedAt());
    long minExpected = before.getTime() + (2L * 60 * 60 * 1000) - 2000;
    long maxExpected = after.getTime() + (2L * 60 * 60 * 1000) + 2000;
    assertTrue(result.snoozedUntil().getTime() >= minExpected);
    assertTrue(result.snoozedUntil().getTime() <= maxExpected);

    verify(repository).save(task);
  }

  @Test
  @DisplayName("Should clear snoozedUntil when status is not SNOOZED")
  void changeStatusShouldClearSnoozeForNonSnoozedStatus() {
    Tasks task = buildTask(4L, TaskStatus.SNOOZED);
    task.setSnoozedUntil(new Timestamp(System.currentTimeMillis() + 3600000));

    when(repository.findById(4L)).thenReturn(Optional.of(task));
    when(repository.countByDharmasUserIdAndStatus(
            eq(task.getDharmas().getUser().getId()), eq(TaskStatus.NOW)))
        .thenReturn(1L);
    when(repository.save(any(Tasks.class))).thenAnswer(invocation -> invocation.getArgument(0));
    stubMapperToDTO();

    TaskDTO result = tasksService.changeStatus(4L, TaskStatus.NOW);

    assertEquals(TaskStatus.NOW, result.status());
    assertNull(result.snoozedUntil());
    assertNotNull(result.updatedAt());

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

  private void stubMapperToDTO() {
    when(tasksMapper.toDTO(any(Tasks.class)))
        .thenAnswer(
            invocation -> {
              Tasks task = invocation.getArgument(0);
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
            });
  }
}
