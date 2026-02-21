package br.com.oriontask.backend.service;

import br.com.oriontask.backend.dto.auth.SignupRequestDTO;
import br.com.oriontask.backend.dto.users.UpdateUserDTO;
import br.com.oriontask.backend.dto.users.UserResponseDTO;
import br.com.oriontask.backend.exceptions.user.UserNotFoundException;
import br.com.oriontask.backend.exceptions.user.UsernameUnavailableException;
import br.com.oriontask.backend.mappers.UsersMapper;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.UsersRepository;
import br.com.oriontask.backend.utils.SecurityUtils;
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
    repository
        .findByUsername(createDTO.username())
        .ifPresent(
            user -> {
              throw new UsernameUnavailableException();
            });

    Users user = mapper.toEntity(createDTO);

    user = repository.save(user);

    return mapper.toDTO(user);
  }

  public UserResponseDTO getMe(Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());

    return mapper.toDTO(repository.findById(userId).orElseThrow(UserNotFoundException::new));
  }

  public UserResponseDTO list(String username, Authentication authentication)
      throws AccessDeniedException {
    Users user = repository.findByUsername(username).orElseThrow(UserNotFoundException::new);

    securityUtils.isOwner(user.getId(), authentication);

    return mapper.toDTO(user);
  }

  @Transactional
  public UserResponseDTO updateProfile(UpdateUserDTO dto, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());

    Users user = getEntity(userId);

    user = mapper.partialUpdate(dto, user);
    user = repository.save(user);

    return mapper.toDTO(user);
  }

  protected Users getEntity(UUID userId) {
    return repository.findById(userId).orElseThrow(UserNotFoundException::new);
  }
}
