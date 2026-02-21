package br.com.oriontask.backend.users.service;

import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.users.dto.UpdateUserDTO;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.exception.UserNotFoundException;
import br.com.oriontask.backend.users.exception.UsernameUnavailableException;
import br.com.oriontask.backend.users.mapper.UsersMapper;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import br.com.oriontask.backend.shared.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import java.nio.file.AccessDeniedException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {
  private final UsersRepository repository;
  private final UsersMapper mapper;
  private final SecurityUtils securityUtils;

  @Transactional
  public UserResponseDTO create(SignupRequestDTO createDTO) {
    log.info("UsersService.create requested username={}", createDTO.username());
    repository
        .findByUsername(createDTO.username())
        .ifPresent(
            user -> {
              log.warn(
                  "UsersService.create blocked: username unavailable username={}",
                  createDTO.username());
              throw new UsernameUnavailableException();
            });

    Users user = mapper.toEntity(createDTO);

    user = repository.save(user);
    log.info(
        "UsersService.create completed userId={} username={}", user.getId(), user.getUsername());

    return mapper.toDTO(user);
  }

  public UserResponseDTO getMe(Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    log.debug("UsersService.getMe requested userId={}", userId);

    return mapper.toDTO(
        repository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("UsersService.getMe user not found userId={}", userId);
                  return new UserNotFoundException();
                }));
  }

  public UserResponseDTO list(String username, Authentication authentication)
      throws AccessDeniedException {
    log.debug("UsersService.list requested username={}", username);
    Users user =
        repository
            .findByUsername(username)
            .orElseThrow(
                () -> {
                  log.warn("UsersService.list user not found username={}", username);
                  return new UserNotFoundException();
                });

    securityUtils.isOwner(user.getId(), authentication);
    log.debug("UsersService.list authorized userId={}", user.getId());

    return mapper.toDTO(user);
  }

  @Transactional
  public UserResponseDTO updateProfile(UpdateUserDTO dto, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    log.info("UsersService.updateProfile requested userId={}", userId);

    Users user = getEntity(userId);

    user = mapper.partialUpdate(dto, user);
    user = repository.save(user);
    log.info("UsersService.updateProfile completed userId={}", userId);

    return mapper.toDTO(user);
  }

  protected Users getEntity(UUID userId) {
    return repository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.warn("UsersService.getEntity user not found userId={}", userId);
              return new UserNotFoundException();
            });
  }
}
