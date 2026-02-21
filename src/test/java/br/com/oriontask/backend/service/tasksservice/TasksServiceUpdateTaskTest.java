package br.com.oriontask.backend.service.tasksservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.shared.utils.DharmaLookupService;
import br.com.oriontask.backend.tasks.dto.TaskDTO;
import br.com.oriontask.backend.tasks.dto.UpdateTaskDTO;
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
class TasksServiceUpdateTaskTest {

  @Mock private TasksRepository repository;
  @Mock private DharmaLookupService dharmaLookup;
  @Mock private TasksMapper tasksMapper;
  @Mock private TaskStatusTransitionPolicy statusPolicy;

  @InjectMocks private TasksService tasksService;

  @Test
  @DisplayName("Should throw when task is not found")
  void updateTaskShouldThrowWhenTaskNotFound() {
    UpdateTaskDTO editDTO = new UpdateTaskDTO("Updated title", "Updated desc", null, null, true);
    UUID userId = UUID.randomUUID();
    when(repository.findByIdAndUserId(99L, userId)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> tasksService.updateTask(editDTO, 99L, userId));

    assertEquals("Task not found", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
    verify(tasksMapper, never()).partialUpdate(any(UpdateTaskDTO.class), any(Tasks.class));
  }

  @Test
  @DisplayName("Should block update when task is DONE")
  void updateTaskShouldThrowWhenTaskIsDone() {
    Tasks existingTask = buildTask(10L, TaskStatus.DONE);
    UpdateTaskDTO editDTO = new UpdateTaskDTO("Updated title", "Updated desc", null, null, true);
    UUID userId = existingTask.getDharmas().getUser().getId();

    when(repository.findByIdAndUserId(10L, userId)).thenReturn(Optional.of(existingTask));
    doThrow(new TaskStatusChangeNotAllowedException())
        .when(statusPolicy)
        .ensureStatusChangeAllowed(existingTask);

    TaskStatusChangeNotAllowedException exception =
        assertThrows(
            TaskStatusChangeNotAllowedException.class,
            () -> tasksService.updateTask(editDTO, 10L, userId));

    assertEquals("Completed tasks cannot change status", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
    verify(tasksMapper, never()).partialUpdate(any(UpdateTaskDTO.class), any(Tasks.class));
  }

  @Test
  @DisplayName("Should update editable task, touch updatedAt and persist")
  void updateTaskShouldUpdateAndSave() {
    Tasks existingTask = buildTask(11L, TaskStatus.NOW);
    Timestamp previousUpdatedAt = existingTask.getUpdatedAt();
    UUID userId = existingTask.getDharmas().getUser().getId();
    UpdateTaskDTO editDTO =
        new UpdateTaskDTO("Rewritten title", "Rewritten description", null, null, true);

    when(repository.findByIdAndUserId(11L, userId)).thenReturn(Optional.of(existingTask));
    when(tasksMapper.partialUpdate(editDTO, existingTask))
        .thenAnswer(invocation -> invocation.getArgument(1));
    when(repository.save(existingTask)).thenAnswer(invocation -> invocation.getArgument(0));
    when(tasksMapper.toDTO(existingTask))
        .thenAnswer(invocation -> toDTO(invocation.getArgument(0)));

    TaskDTO result = tasksService.updateTask(editDTO, 11L, userId);

    assertNotNull(result);
    assertEquals(11L, result.id());
    assertNotNull(existingTask.getUpdatedAt());
    assertNotNull(result.updatedAt());
    assertEquals(existingTask.getUpdatedAt(), result.updatedAt());
    assertNotNull(previousUpdatedAt);
    assertNotNull(existingTask.getUpdatedAt());
    assertEquals(TaskStatus.NOW, result.status());

    verify(tasksMapper).partialUpdate(editDTO, existingTask);
    verify(repository).save(existingTask);
    verify(tasksMapper).toDTO(existingTask);
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
