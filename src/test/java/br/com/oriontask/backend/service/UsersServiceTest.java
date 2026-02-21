package br.com.oriontask.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.users.dto.UpdateUserDTO;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.exception.UserNotFoundException;
import br.com.oriontask.backend.users.exception.UsernameUnavailableException;
import br.com.oriontask.backend.users.mapper.UsersMapper;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import br.com.oriontask.backend.users.service.UsersService;
import br.com.oriontask.backend.shared.utils.SecurityUtils;
import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

  @Mock private UsersRepository repository;
  @Mock private UsersMapper mapper;
  @Mock private SecurityUtils securityUtils;
  @Mock private Authentication authentication;

  @InjectMocks private UsersService usersService;

  @Test
  @DisplayName("Should throw when username is unavailable on create")
  void createShouldThrowWhenUsernameUnavailable() {
    SignupRequestDTO dto =
        new SignupRequestDTO("Test User", "taken_user", "test@example.com", "Strong123!");
    when(repository.findByUsername("taken_user"))
        .thenReturn(Optional.of(Users.builder().id(UUID.randomUUID()).build()));

    UsernameUnavailableException exception =
        assertThrows(UsernameUnavailableException.class, () -> usersService.create(dto));

    assertEquals("Username unavailable", exception.getMessage());
    verify(repository, never()).save(ArgumentMatchers.any(Users.class));
  }

  @Test
  @DisplayName("Should save and return mapped response on create")
  void createShouldPersistUserAndReturnDTO() {
    UUID userId = UUID.randomUUID();
    SignupRequestDTO dto =
        new SignupRequestDTO("Test User", "test_user", "test@example.com", "Strong123!");
    Users entity =
        Users.builder().name("Test User").username("test_user").email("test@example.com").build();

    Users saved = Users.builder().id(userId).name("Test User").username("test_user").build();
    UserResponseDTO response =
        new UserResponseDTO(
            userId, "Test User", "test_user", null, new Timestamp(1), new Timestamp(2));

    when(repository.findByUsername("test_user")).thenReturn(Optional.empty());
    when(mapper.toEntity(dto)).thenReturn(entity);
    org.mockito.Mockito.doAnswer(
            invocation -> {
              Users arg = invocation.getArgument(0);
              assertNotNull(arg);
              assertEquals("test_user", arg.getUsername());
              return saved;
            })
        .when(repository)
        .save(ArgumentMatchers.any());
    when(mapper.toDTO(saved)).thenReturn(response);

    UserResponseDTO result = usersService.create(dto);

    assertEquals(userId, result.id());
    assertEquals("test_user", result.username());
    verify(repository).save(ArgumentMatchers.any(Users.class));
    verify(mapper).toDTO(saved);
  }

  @Test
  @DisplayName("Should return current authenticated user on getMe")
  void getMeShouldReturnAuthenticatedUser() {
    UUID userId = UUID.randomUUID();
    Users user =
        Users.builder()
            .id(userId)
            .name("Test User")
            .username("test_user")
            .email("test@example.com")
            .build();
    UserResponseDTO response =
        new UserResponseDTO(
            userId,
            "Test User",
            "test_user",
            "test@example.com",
            new Timestamp(1),
            new Timestamp(2));

    when(authentication.getName()).thenReturn(userId.toString());
    when(repository.findById(userId)).thenReturn(Optional.of(user));
    when(mapper.toDTO(user)).thenReturn(response);

    UserResponseDTO result = usersService.getMe(authentication);

    assertEquals(userId, result.id());
    assertEquals("test_user", result.username());
  }

  @Test
  @DisplayName("Should throw when authenticated user is not found on getMe")
  void getMeShouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();

    when(authentication.getName()).thenReturn(userId.toString());
    when(repository.findById(userId)).thenReturn(Optional.empty());

    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> usersService.getMe(authentication));

    assertEquals("User not found", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw when username is not found on list")
  void listShouldThrowWhenUserNotFound() {
    when(repository.findByUsername("missing_user")).thenReturn(Optional.empty());

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> usersService.list("missing_user", authentication));

    assertEquals("User not found", exception.getMessage());
    verify(mapper, never()).toDTO(ArgumentMatchers.any(Users.class));
  }

  @Test
  @DisplayName("Should throw when authenticated user is not owner on list")
  void listShouldThrowWhenNotOwner() throws AccessDeniedException {
    UUID userId = UUID.randomUUID();
    Users user = Users.builder().id(userId).username("target_user").build();

    when(repository.findByUsername("target_user")).thenReturn(Optional.of(user));
    org.mockito.Mockito.doThrow(new AccessDeniedException("Invalid authentication principal"))
        .when(securityUtils)
        .isOwner(userId, authentication);

    AccessDeniedException exception =
        assertThrows(
            AccessDeniedException.class, () -> usersService.list("target_user", authentication));

    assertEquals("Invalid authentication principal", exception.getMessage());
    verify(mapper, never()).toDTO(ArgumentMatchers.any(Users.class));
  }

  @Test
  @DisplayName("Should return user when requester is owner on list")
  void listShouldReturnUserWhenOwner() throws AccessDeniedException {
    UUID userId = UUID.randomUUID();
    Users user =
        Users.builder()
            .id(userId)
            .name("Test User")
            .username("test_user")
            .email("test@example.com")
            .build();
    UserResponseDTO response =
        new UserResponseDTO(
            userId,
            "Test User",
            "test_user",
            "test@example.com",
            new Timestamp(1),
            new Timestamp(2));

    when(repository.findByUsername("test_user")).thenReturn(Optional.of(user));
    when(mapper.toDTO(user)).thenReturn(response);

    UserResponseDTO result = usersService.list("test_user", authentication);

    assertEquals(userId, result.id());
    verify(securityUtils).isOwner(userId, authentication);
    verify(mapper).toDTO(user);
  }

  @Test
  @DisplayName("Should update profile and return mapped response")
  void updateProfileShouldPersistChangesAndReturnDTO() {
    UUID userId = UUID.randomUUID();
    UpdateUserDTO dto = new UpdateUserDTO("Updated Name", "updateduser", "updated@example.com");
    Users existing =
        Users.builder()
            .id(userId)
            .name("Old Name")
            .username("olduser")
            .email("old@example.com")
            .build();
    Users updated =
        Users.builder()
            .id(userId)
            .name("Updated Name")
            .username("updateduser")
            .email("updated@example.com")
            .build();
    UserResponseDTO response =
        new UserResponseDTO(
            userId,
            "Updated Name",
            "updateduser",
            "updated@example.com",
            new Timestamp(1),
            new Timestamp(2));

    when(authentication.getName()).thenReturn(userId.toString());
    when(repository.findById(userId)).thenReturn(Optional.of(existing));
    when(mapper.partialUpdate(dto, existing)).thenReturn(updated);
    when(repository.save(updated)).thenReturn(updated);
    when(mapper.toDTO(updated)).thenReturn(response);

    UserResponseDTO result = usersService.updateProfile(dto, authentication);

    assertEquals(userId, result.id());
    assertEquals("updateduser", result.username());
    verify(mapper).partialUpdate(dto, existing);
    verify(repository).save(updated);
    verify(mapper).toDTO(updated);
  }

  @Test
  @DisplayName("Should throw when updating profile for non-existent user")
  void updateProfileShouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();

    when(authentication.getName()).thenReturn(userId.toString());
    when(repository.findById(userId)).thenReturn(Optional.empty());

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class,
            () ->
                usersService.updateProfile(
                    new UpdateUserDTO("Updated Name", "updateduser", "updated@example.com"),
                    authentication));

    assertEquals("User not found", exception.getMessage());
    verify(repository, never()).save(ArgumentMatchers.any(Users.class));
  }
}
