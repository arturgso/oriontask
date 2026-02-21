package br.com.oriontask.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dto.tasks.TaskDTO;
import br.com.oriontask.backend.dto.tasks.UpdateTaskDTO;
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
class TasksServiceUpdateTaskTest {

  @Mock private TasksRepository repository;
  @Mock private DharmasRepository dharmasRepository;
  @Mock private TasksMapper tasksMapper;
  @Mock private TaskStatusTransitionPolicy statusPolicy;

  @InjectMocks private TasksService tasksService;

  @Test
  @DisplayName("Should throw when task is not found")
  void updateTaskShouldThrowWhenTaskNotFound() {
    UpdateTaskDTO editDTO = new UpdateTaskDTO("Updated title", "Updated desc", null, null, true);
    when(repository.findById(99L)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> tasksService.updateTask(editDTO, 99L));

    assertEquals("Task not found", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
    verify(tasksMapper, never()).partialUpdate(any(UpdateTaskDTO.class), any(Tasks.class));
  }

  @Test
  @DisplayName("Should block update when task is DONE")
  void updateTaskShouldThrowWhenTaskIsDone() {
    Tasks existingTask = buildTask(10L, TaskStatus.DONE);
    UpdateTaskDTO editDTO = new UpdateTaskDTO("Updated title", "Updated desc", null, null, true);

    when(repository.findById(10L)).thenReturn(Optional.of(existingTask));
    doThrow(new TaskAlreadyCompletedException())
        .when(statusPolicy)
        .ensureStatusChangeAllowed(existingTask);

    TaskAlreadyCompletedException exception =
        assertThrows(
            TaskAlreadyCompletedException.class, () -> tasksService.updateTask(editDTO, 10L));

    assertEquals("Task is already completed", exception.getMessage());
    verify(repository, never()).save(any(Tasks.class));
    verify(tasksMapper, never()).partialUpdate(any(UpdateTaskDTO.class), any(Tasks.class));
  }

  @Test
  @DisplayName("Should update editable task, touch updatedAt and persist")
  void updateTaskShouldUpdateAndSave() {
    Tasks existingTask = buildTask(11L, TaskStatus.NOW);
    Timestamp previousUpdatedAt = existingTask.getUpdatedAt();
    UpdateTaskDTO editDTO =
        new UpdateTaskDTO("Rewritten title", "Rewritten description", null, null, true);

    when(repository.findById(11L)).thenReturn(Optional.of(existingTask));
    when(tasksMapper.partialUpdate(editDTO, existingTask))
        .thenAnswer(invocation -> invocation.getArgument(1));
    when(repository.save(existingTask)).thenAnswer(invocation -> invocation.getArgument(0));
    when(tasksMapper.toDTO(existingTask))
        .thenAnswer(invocation -> toDTO(invocation.getArgument(0)));

    TaskDTO result = tasksService.updateTask(editDTO, 11L);

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
