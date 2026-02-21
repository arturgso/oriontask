package br.com.oriontask.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dto.tasks.TaskDTO;
import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.exceptions.task.TaskAlreadyCompletedException;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TasksServiceMarkAsDoneTest {

  @Mock private TasksRepository repository;
  @Mock private DharmasRepository dharmasRepository;
  @Mock private TasksMapper tasksMapper;
  @Mock private TaskStatusTransitionPolicy statusPolicy;

  @InjectMocks private TasksService tasksService;

  @Test
  @DisplayName("Should throw when task is not found")
  void markAsDoneShouldThrowWhenTaskNotFound() {
    when(repository.findById(77L)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> tasksService.markAsDone(77L));

    assertEquals("Task not found", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
  }

  @Test
  @DisplayName("Should throw when task is already completed")
  void markAsDoneShouldThrowWhenTaskAlreadyCompleted() {
    Tasks task = buildTask(78L, TaskStatus.DONE);
    when(repository.findById(78L)).thenReturn(Optional.of(task));
    doThrow(new TaskAlreadyCompletedException()).when(statusPolicy).markAsDone(task);

    TaskAlreadyCompletedException exception =
        assertThrows(TaskAlreadyCompletedException.class, () -> tasksService.markAsDone(78L));

    assertEquals("Task is already completed", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
  }

  @Test
  @DisplayName("Should mark task as done and persist")
  void markAsDoneShouldPersist() {
    Tasks task = buildTask(79L, TaskStatus.NOW);

    when(repository.findById(79L)).thenReturn(Optional.of(task));
    doAnswer(
            invocation -> {
              Tasks target = invocation.getArgument(0);
              target.setStatus(TaskStatus.DONE);
              target.setCompletedAt(new Timestamp(System.currentTimeMillis()));
              target.setSnoozedUntil(null);
              return null;
            })
        .when(statusPolicy)
        .markAsDone(task);
    when(repository.save(task)).thenReturn(task);
    when(tasksMapper.toDTO(task)).thenAnswer(invocation -> toDTO(invocation.getArgument(0)));

    TaskDTO result = tasksService.markAsDone(79L);

    assertNotNull(result);
    assertEquals(TaskStatus.DONE, result.status());
    assertNotNull(result.completedAt());

    verify(statusPolicy).markAsDone(task);
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
