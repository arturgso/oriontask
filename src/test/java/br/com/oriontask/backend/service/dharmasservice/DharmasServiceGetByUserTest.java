package br.com.oriontask.backend.service.dharmasservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dto.dharmas.DharmasDTO;
import br.com.oriontask.backend.mappers.DharmasMapper;
import br.com.oriontask.backend.model.Dharmas;
import br.com.oriontask.backend.policy.DharmasPolicy;
import br.com.oriontask.backend.repository.DharmasRepository;
import br.com.oriontask.backend.repository.TasksRepository;
import br.com.oriontask.backend.service.DharmasService;
import br.com.oriontask.backend.service.user.UserLookupService;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DharmasServiceGetByUserTest {

  @Mock private DharmasRepository repository;
  @Mock private UserLookupService userLookup;
  @Mock private TasksRepository tasksRepository;
  @Mock private DharmasMapper dharmasMapper;
  @Mock private DharmasPolicy dharmasPolicy;

  @InjectMocks private DharmasService dharmasService;

  @Test
  @DisplayName("Should fetch all dharmas when includeHidden is true")
  void getDharmasByUserShouldIncludeHidden() {
    UUID userId = UUID.randomUUID();
    List<Dharmas> dharmas = List.of(Dharmas.builder().id(1L).name("Body").hidden(true).build());
    List<DharmasDTO> expected =
        List.of(new DharmasDTO(1L, "Body", "#123456", true, new Timestamp(1), new Timestamp(2)));

    when(repository.findByUserId(userId)).thenReturn(dharmas);
    when(dharmasMapper.toDTO(dharmas)).thenReturn(expected);

    List<DharmasDTO> result = dharmasService.getDharmasByUser(userId.toString(), true);

    assertEquals(1, result.size());
    verify(repository).findByUserId(userId);
    verify(dharmasMapper).toDTO(dharmas);
  }

  @Test
  @DisplayName("Should fetch only visible dharmas when includeHidden is false")
  void getDharmasByUserShouldExcludeHidden() {
    UUID userId = UUID.randomUUID();
    List<Dharmas> dharmas = List.of(Dharmas.builder().id(2L).name("Mind").hidden(false).build());
    List<DharmasDTO> expected =
        List.of(new DharmasDTO(2L, "Mind", "#654321", false, new Timestamp(1), new Timestamp(2)));

    when(repository.findByUserIdAndHiddenFalse(userId)).thenReturn(dharmas);
    when(dharmasMapper.toDTO(dharmas)).thenReturn(expected);

    List<DharmasDTO> result = dharmasService.getDharmasByUser(userId.toString(), false);

    assertEquals(1, result.size());
    verify(repository).findByUserIdAndHiddenFalse(userId);
    verify(dharmasMapper).toDTO(dharmas);
  }

  @Test
  @DisplayName("Should throw for invalid user id format")
  void getDharmasByUserShouldThrowForInvalidUuid() {
    assertThrows(
        IllegalArgumentException.class, () -> dharmasService.getDharmasByUser("not-a-uuid", true));
  }
}
