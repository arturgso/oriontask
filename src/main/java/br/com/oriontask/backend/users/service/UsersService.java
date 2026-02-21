package br.com.oriontask.backend.users.service;

import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.users.dto.UpdateUserDTO;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.exception.UserNotFoundException;
import br.com.oriontask.backend.users.mapper.UsersMapper;
import br.com.oriontask.backend.users.model.Users;
import br.com.oriontask.backend.users.repository.UsersRepository;
import jakarta.transaction.Transactional;
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

  @Transactional
  public UserResponseDTO create(SignupRequestDTO createDTO) {
    log.info("UsersService.create requested email={}", createDTO.email());
    repository
        .findByEmail(createDTO.email())
        .ifPresent(
            user -> {
              log.warn(
                  "UsersService.create blocked: email unavailable email={}", createDTO.email());
              throw new IllegalArgumentException("Email unavailable");
            });

    Users user = mapper.toEntity(createDTO);

    user = repository.save(user);
    log.info("UsersService.create completed userId={} email={}", user.getId(), user.getEmail());

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

  @Transactional
  public UserResponseDTO updateProfile(UpdateUserDTO dto, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    log.info("UsersService.update requested userId={}", userId);

    Users user = getEntity(userId);

    user = mapper.partialUpdate(dto, user);
    user = repository.save(user);
    log.info("UsersService.update completed userId={}", userId);

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
