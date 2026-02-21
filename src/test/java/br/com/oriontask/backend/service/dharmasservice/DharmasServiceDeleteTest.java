package br.com.oriontask.backend.service.dharmasservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.dharmas.mapper.DharmasMapper;
import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.tasks.model.Tasks;
import br.com.oriontask.backend.dharmas.policy.DharmasPolicy;
import br.com.oriontask.backend.dharmas.repository.DharmasRepository;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import br.com.oriontask.backend.dharmas.service.DharmasService;
import br.com.oriontask.backend.users.service.UserLookupService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DharmasServiceDeleteTest {

  @Mock private DharmasRepository repository;
  @Mock private UserLookupService userLookup;
  @Mock private TasksRepository tasksRepository;
  @Mock private DharmasMapper dharmasMapper;
  @Mock private DharmasPolicy dharmasPolicy;

  @InjectMocks private DharmasService dharmasService;

  @Test
  @DisplayName("Should throw when dharmas does not exist")
  void deleteShouldThrowWhenDharmasNotFound() {
    when(repository.findById(60L)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> dharmasService.deleteDharmas(60L));

    assertEquals("Dharmas not found", exception.getMessage());
    verify(repository, never()).delete(any(Dharmas.class));
  }

  @Test
  @DisplayName("Should block delete when there are active tasks")
  void deleteShouldThrowWhenActiveTasksExist() {
    Dharmas dharmas = Dharmas.builder().id(61L).build();
    Tasks doneTask = Tasks.builder().id(1L).status(TaskStatus.DONE).build();
    Tasks nowTask = Tasks.builder().id(2L).status(TaskStatus.NOW).build();

    when(repository.findById(61L)).thenReturn(Optional.of(dharmas));
    when(tasksRepository.findByDharmasId(61L, Pageable.unpaged()))
        .thenReturn(new PageImpl<>(List.of(doneTask, nowTask)));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> dharmasService.deleteDharmas(61L));

    assertEquals(
        "Cannot delete Dharmas with active tasks. Complete or move tasks first.",
        exception.getMessage());
    verify(repository, never()).delete(dharmas);
  }

  @Test
  @DisplayName("Should delete when all tasks are DONE")
  void deleteShouldSucceedWhenNoActiveTasks() {
    Dharmas dharmas = Dharmas.builder().id(62L).build();
    Tasks done1 = Tasks.builder().id(1L).status(TaskStatus.DONE).build();
    Tasks done2 = Tasks.builder().id(2L).status(TaskStatus.DONE).build();

    when(repository.findById(62L)).thenReturn(Optional.of(dharmas));
    when(tasksRepository.findByDharmasId(62L, Pageable.unpaged()))
        .thenReturn(new PageImpl<>(List.of(done1, done2)));

    dharmasService.deleteDharmas(62L);

    verify(repository).delete(dharmas);
  }
}
