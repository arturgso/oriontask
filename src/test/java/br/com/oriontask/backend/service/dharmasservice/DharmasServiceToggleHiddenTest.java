package br.com.oriontask.backend.service.dharmasservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dharmas.mapper.DharmasMapper;
import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.dharmas.policy.DharmasPolicy;
import br.com.oriontask.backend.dharmas.repository.DharmasRepository;
import br.com.oriontask.backend.dharmas.service.DharmasService;
import br.com.oriontask.backend.shared.utils.UserLookupService;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    UUID userId = UUID.randomUUID();
    when(repository.findByIdAndUserId(70L, userId)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> dharmasService.toggleHidden(70L, userId));

    assertEquals("Dharmas not found", exception.getMessage());
  }

  @Test
  @DisplayName("Should toggle hidden in dharmas")
  void toggleHiddenShouldToggleDharmasHiddenFlag() {
    UUID userId = UUID.randomUUID();
    Dharmas dharmas = Dharmas.builder().id(71L).hidden(false).build();

    when(repository.findByIdAndUserId(71L, userId)).thenReturn(Optional.of(dharmas));

    dharmasService.toggleHidden(71L, userId);

    assertTrue(dharmas.getHidden());
    verifyNoInteractions(tasksRepository);
  }
}
