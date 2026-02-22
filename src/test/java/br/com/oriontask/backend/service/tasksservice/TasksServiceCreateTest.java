package br.com.oriontask.backend.service.tasksservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.shared.utils.DharmaLookupService;
import br.com.oriontask.backend.tasks.dto.NewTaskDTO;
import br.com.oriontask.backend.tasks.dto.TaskDTO;
import br.com.oriontask.backend.tasks.mapper.TasksMapper;
import br.com.oriontask.backend.tasks.model.Tasks;
import br.com.oriontask.backend.tasks.policy.TaskStatusTransitionPolicy;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import br.com.oriontask.backend.tasks.service.TasksService;
import br.com.oriontask.backend.users.model.Users;
import java.sql.Timestamp;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TasksServiceCreateTest {

  @Mock private TasksRepository repository;
  @Mock private DharmaLookupService dharmaLookup;
  @Mock private TasksMapper tasksMapper;
  @Mock private TaskStatusTransitionPolicy statusPolicy;

  @InjectMocks private TasksService tasksService;

  @Test
  @DisplayName("Should create task with NOW when user is below NOW limit")
  void createShouldSetNowWhenLimitNotReached() {
    UUID userId = UUID.randomUUID();
    Dharmas dharma = buildDharma(userId, 10L, false);
    NewTaskDTO createDTO = new NewTaskDTO(10L, "Nova tarefa", "Desc", null, null);
    Tasks mapped = Tasks.builder().title("Nova tarefa").description("Desc").build();

    when(dharmaLookup.getRequiredDharma(10L, userId)).thenReturn(dharma);
    when(tasksMapper.toEntity(createDTO)).thenReturn(mapped);
    when(repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW)).thenReturn(4L);
    when(repository.save(mapped)).thenReturn(mapped);
    when(tasksMapper.toDTO(mapped)).thenAnswer(invocation -> toDTO(invocation.getArgument(0)));

    TaskDTO result = tasksService.create(createDTO, userId);

    assertEquals(TaskStatus.NOW, result.status());
    verify(repository).save(mapped);
  }

  @Test
  @DisplayName("Should create task with WAITING when NOW limit is reached")
  void createShouldSetWaitingWhenLimitReached() {
    UUID userId = UUID.randomUUID();
    Dharmas dharma = buildDharma(userId, 11L, true);
    NewTaskDTO createDTO = new NewTaskDTO(11L, "Outra tarefa", "Desc", null, null);
    Tasks mapped = Tasks.builder().title("Outra tarefa").description("Desc").build();

    when(dharmaLookup.getRequiredDharma(11L, userId)).thenReturn(dharma);
    when(tasksMapper.toEntity(createDTO)).thenReturn(mapped);
    when(repository.countByDharmasUserIdAndStatus(userId, TaskStatus.NOW)).thenReturn(5L);
    when(repository.save(mapped)).thenReturn(mapped);
    when(tasksMapper.toDTO(mapped)).thenAnswer(invocation -> toDTO(invocation.getArgument(0)));

    TaskDTO result = tasksService.create(createDTO, userId);

    assertEquals(TaskStatus.WAITING, result.status());
    verify(repository).save(mapped);
  }

  private Dharmas buildDharma(UUID userId, Long dharmaId, boolean hidden) {
    Users user = Users.builder().id(userId).build();
    return Dharmas.builder().id(dharmaId).user(user).hidden(hidden).build();
  }

  private TaskDTO toDTO(Tasks task) {
    return new TaskDTO(
        1L,
        task.getDharmas().getId(),
        task.getTitle(),
        task.getDescription(),
        task.getKarmaType(),
        task.getEffortLevel(),
        task.getStatus(),
        task.getHidden(),
        task.getCompletedAt(),
        task.getSnoozedUntil(),
        task.getCreatedAt() == null
            ? new Timestamp(System.currentTimeMillis())
            : task.getCreatedAt(),
        task.getUpdatedAt() == null
            ? new Timestamp(System.currentTimeMillis())
            : task.getUpdatedAt());
  }
}
