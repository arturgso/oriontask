package br.com.oriontask.backend.service.dharmasservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.dharmas.dto.DharmasDTO;
import br.com.oriontask.backend.dharmas.dto.NewDharmasDTO;
import br.com.oriontask.backend.dharmas.mapper.DharmasMapper;
import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.dharmas.policy.DharmasPolicy;
import br.com.oriontask.backend.dharmas.repository.DharmasRepository;
import br.com.oriontask.backend.dharmas.service.DharmasService;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import br.com.oriontask.backend.users.exception.UserLookupExceptionImpl;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.service.UserLookupService;
import java.sql.Timestamp;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DharmasServiceCreateTest {

  @Mock private DharmasRepository repository;
  @Mock private UserLookupService userLookup;
  @Mock private TasksRepository tasksRepository;
  @Mock private DharmasMapper dharmasMapper;
  @Mock private DharmasPolicy dharmasPolicy;

  @InjectMocks private DharmasService dharmasService;

  @Test
  @DisplayName("Should throw when user does not exist")
  void createShouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userLookup.getRequiredUse(userId)).thenThrow(new UserLookupExceptionImpl());

    IllegalArgumentException exception =
        assertThrows(
            UserLookupExceptionImpl.class,
            () -> dharmasService.create(new NewDharmasDTO("Focus", "#112233"), userId));

    assertEquals("User not found", exception.getMessage());
    verify(repository, never()).save(any(Dharmas.class));
  }

  @Test
  @DisplayName("Should throw when user reached dharmas limit")
  void createShouldThrowWhenLimitReached() {
    UUID userId = UUID.randomUUID();
    Users user = Users.builder().id(userId).build();

    when(userLookup.getRequiredUse(userId)).thenReturn(user);
    when(repository.countByUser(user)).thenReturn(8L);
    doThrow(new IllegalStateException("Maximum number of dharmas reached for this user"))
        .when(dharmasPolicy)
        .validateMaxDharmasPerUser(8L);

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> dharmasService.create(new NewDharmasDTO("Focus", "#112233"), userId));

    assertEquals("Maximum number of dharmas reached for this user", exception.getMessage());
    verify(repository, never()).save(any(Dharmas.class));
  }

  @Test
  @DisplayName("Should create dharma with explicit color")
  void createShouldPersistWithGivenColor() {
    UUID userId = UUID.randomUUID();
    Users user = Users.builder().id(userId).build();
    NewDharmasDTO dto = new NewDharmasDTO("Health", "#abcdef");
    Dharmas entity = Dharmas.builder().name("Health").color("#abcdef").build();

    when(userLookup.getRequiredUse(userId)).thenReturn(user);
    when(repository.countByUser(user)).thenReturn(2L);
    when(dharmasMapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(dharmasMapper.toDTO(entity))
        .thenReturn(
            new DharmasDTO(1L, "Health", "#abcdef", false, new Timestamp(1), new Timestamp(2)));

    DharmasDTO result = dharmasService.create(dto, userId);

    assertNotNull(result);
    assertEquals("Health", result.name());
    assertEquals("#abcdef", result.color());
    assertEquals(user, entity.getUser());
    verify(repository).save(entity);
  }

  @Test
  @DisplayName("Should create dharma with generated color when color is null")
  void createShouldGenerateColorWhenMissing() {
    UUID userId = UUID.randomUUID();
    Users user = Users.builder().id(userId).build();
    NewDharmasDTO dto = new NewDharmasDTO("Work", null);
    Dharmas entity = Dharmas.builder().name("Work").color(null).build();

    when(userLookup.getRequiredUse(userId)).thenReturn(user);
    when(repository.countByUser(user)).thenReturn(1L);
    when(dharmasMapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenAnswer(invocation -> invocation.getArgument(0));
    when(dharmasMapper.toDTO(entity))
        .thenAnswer(
            invocation -> {
              Dharmas d = invocation.getArgument(0);
              return new DharmasDTO(
                  2L, d.getName(), d.getColor(), false, new Timestamp(1), new Timestamp(2));
            });

    DharmasDTO result = dharmasService.create(dto, userId);

    assertNotNull(result.color());
    assertNotNull(entity.getColor());
    verify(repository).save(entity);
  }
}
