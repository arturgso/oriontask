package br.com.oriontask.backend.service.tasksservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.shared.utils.DharmaLookupService;
import br.com.oriontask.backend.tasks.mapper.TasksMapper;
import br.com.oriontask.backend.tasks.model.Tasks;
import br.com.oriontask.backend.tasks.policy.TaskStatusTransitionPolicy;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import br.com.oriontask.backend.tasks.service.TasksService;
import br.com.oriontask.backend.users.model.Users;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TasksServicePromoteWaitingTest {

  @Mock private TasksRepository repository;
  @Mock private DharmaLookupService dharmaLookup;
  @Mock private TasksMapper tasksMapper;
  @Mock private TaskStatusTransitionPolicy statusPolicy;

  @InjectMocks private TasksService tasksService;

  @Test
  @DisplayName("Should promote most recent WAITING task when NOW count is below limit")
  void shouldPromoteWaitingTaskWhenCapacityExists() {
    UUID userId = UUID.randomUUID();
    Tasks waitingTask = buildTask(1L, userId, TaskStatus.WAITING);

    when(repository.findDistinctUserIdsByStatus(TaskStatus.WAITING)).thenReturn(List.of(userId));
    when(repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW)).thenReturn(3L);
    when(repository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, TaskStatus.WAITING))
        .thenReturn(Optional.of(waitingTask));
    when(repository.save(waitingTask)).thenReturn(waitingTask);

    tasksService.promoteRecentWaitingTasksToNow();

    verify(statusPolicy).markAsNow(waitingTask);
    verify(repository).save(waitingTask);
  }

  @Test
  @DisplayName("Should not promote when NOW count is already at limit")
  void shouldNotPromoteWhenNoCapacity() {
    UUID userId = UUID.randomUUID();

    when(repository.findDistinctUserIdsByStatus(TaskStatus.WAITING)).thenReturn(List.of(userId));
    when(repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW)).thenReturn(5L);

    tasksService.promoteRecentWaitingTasksToNow();

    verify(repository, never())
        .findFirstByUserIdAndStatusOrderByCreatedAtDesc(any(), any(TaskStatus.class));
    verify(statusPolicy, never()).markAsNow(any(Tasks.class));
    verify(repository, never()).save(any(Tasks.class));
  }

  private Tasks buildTask(Long taskId, UUID userId, TaskStatus status) {
    Users user = Users.builder().id(userId).build();
    Dharmas dharmas = Dharmas.builder().id(10L).user(user).build();
    return Tasks.builder()
        .id(taskId)
        .user(user)
        .dharmas(dharmas)
        .title("Task title")
        .description("Task desc")
        .status(status)
        .createdAt(new Timestamp(System.currentTimeMillis() - 10000))
        .updatedAt(new Timestamp(System.currentTimeMillis() - 10000))
        .build();
  }
}
