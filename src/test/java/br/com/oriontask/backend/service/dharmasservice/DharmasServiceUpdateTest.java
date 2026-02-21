package br.com.oriontask.backend.service.dharmasservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dharmas.dto.DharmasDTO;
import br.com.oriontask.backend.dharmas.dto.UpdateDharmasDTO;
import br.com.oriontask.backend.dharmas.mapper.DharmasMapper;
import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.dharmas.policy.DharmasPolicy;
import br.com.oriontask.backend.dharmas.repository.DharmasRepository;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import br.com.oriontask.backend.dharmas.service.DharmasService;
import br.com.oriontask.backend.users.service.UserLookupService;
import java.sql.Timestamp;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DharmasServiceUpdateTest {

  @Mock private DharmasRepository repository;
  @Mock private UserLookupService userLookup;
  @Mock private TasksRepository tasksRepository;
  @Mock private DharmasMapper dharmasMapper;
  @Mock private DharmasPolicy dharmasPolicy;

  @InjectMocks private DharmasService dharmasService;

  @Test
  @DisplayName("Should throw when dharmas does not exist")
  void updateShouldThrowWhenDharmasNotFound() {
    when(repository.findById(50L)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> dharmasService.updateDharmas(new UpdateDharmasDTO("x", "#123456"), 50L));

    assertEquals("Dharmas not found", exception.getMessage());
    verify(repository, never()).save(any(Dharmas.class));
  }

  @Test
  @DisplayName("Should partially update and save dharmas")
  void updateShouldPersistChanges() {
    Dharmas dharmas = Dharmas.builder().id(51L).name("Body").color("#111111").hidden(false).build();
    UpdateDharmasDTO dto = new UpdateDharmasDTO("Mind", "#222222");

    when(repository.findById(51L)).thenReturn(Optional.of(dharmas));
    when(dharmasMapper.partialUpdate(dto, dharmas))
        .thenAnswer(
            invocation -> {
              Dharmas target = invocation.getArgument(1);
              target.setName("Mind");
              target.setColor("#222222");
              return target;
            });
    when(repository.save(dharmas)).thenReturn(dharmas);
    when(dharmasMapper.toDTO(dharmas))
        .thenReturn(
            new DharmasDTO(51L, "Mind", "#222222", false, new Timestamp(1), new Timestamp(2)));

    DharmasDTO result = dharmasService.updateDharmas(dto, 51L);

    assertNotNull(result);
    assertEquals("Mind", result.name());
    assertEquals("#222222", result.color());
    verify(repository).save(dharmas);
  }
}
