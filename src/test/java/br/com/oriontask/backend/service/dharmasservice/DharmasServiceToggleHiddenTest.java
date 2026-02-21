package br.com.oriontask.backend.service.dharmasservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class DharmasServiceToggleHiddenTest {

  @Mock private DharmasRepository repository;
  @Mock private UserLookupService userLookup;
  @Mock private TasksRepository tasksRepository;
  @Mock private DharmasMapper dharmasMapper;
  @Mock private DharmasPolicy dharmasPolicy;

  @InjectMocks private DharmasService dharmasService;

  @Test
  @DisplayName("Should throw when dharmas does not exist")
  void toggleHiddenShouldThrowWhenDharmasNotFound() {
    when(repository.findById(70L)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> dharmasService.toggleHidden(70L));

    assertEquals("Dharmas not found", exception.getMessage());
  }

  @Test
  @DisplayName("Should toggle hidden and propagate value to all tasks")
  void toggleHiddenShouldPropagateToTasks() {
    Dharmas dharmas = Dharmas.builder().id(71L).hidden(false).build();
    Tasks task1 = Tasks.builder().id(1L).hidden(false).build();
    Tasks task2 = Tasks.builder().id(2L).hidden(false).build();

    when(repository.findById(71L)).thenReturn(Optional.of(dharmas));
    when(tasksRepository.findByDharmasId(71L, Pageable.unpaged()))
        .thenReturn(new PageImpl<>(List.of(task1, task2)));

    dharmasService.toggleHidden(71L);

    assertEquals(true, dharmas.getHidden());
    assertEquals(true, task1.getHidden());
    assertEquals(true, task2.getHidden());
    verify(tasksRepository).saveAll(any(Iterable.class));
  }
}
