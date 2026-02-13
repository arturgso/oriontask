package br.com.oriontask.backend.service;

import br.com.oriontask.backend.dto.auth.SignupRequestDTO;
import br.com.oriontask.backend.dto.users.UpdateUserDTO;
import br.com.oriontask.backend.dto.users.UserResponseDTO;
import br.com.oriontask.backend.mappers.UsersMapper;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.UsersRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {
  private final UsersRepository repository;
  private final UsersMapper mapper;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserResponseDTO create(SignupRequestDTO createDTO) {
    repository
        .findByUsername(createDTO.username())
        .ifPresent(
            user -> {
              throw new IllegalArgumentException("Username unavailable");
            });

    Users user = Users.builder().name(createDTO.name()).username(createDTO.username()).build();

    user = repository.save(user);

    return mapper.toDTO(user);
  }

  public UserResponseDTO list(String id) {
    return mapper.toDTO(
        repository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("User not found")));
  }

  public UserResponseDTO getByUsername(String username) {
    return mapper.toDTO(
        repository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found")));
  }

  public UserResponseDTO getProfile(UUID userId) {
    return mapper.toDTO(
        repository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found")));
  }

  @Transactional
  public UserResponseDTO updateProfile(UUID userId, UpdateUserDTO dto) {
    Users user =
        repository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user = mapper.partialUpdate(dto, user);
    user = repository.save(user);

    return mapper.toDTO(user);
  }
}
