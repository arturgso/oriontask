package br.com.oriontask.backend.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.oriontask.backend.dto.EditUserDTO;
import br.com.oriontask.backend.dto.ProfileResponseDTO;
import br.com.oriontask.backend.dto.UpdateProfileDTO;
import br.com.oriontask.backend.dto.UserResponseDTO;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {
    private final UsersRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO create(EditUserDTO createDTO) {
        repository.findByUsername(createDTO.username()).ifPresent(user -> {
            throw new IllegalArgumentException("Username unavailable");
        });

        Users user = Users.builder()
                .name(createDTO.name())
                .username(createDTO.username())
                .build();

        user = repository.save(user);

        return createResponseDTO(user);
    }

    public UserResponseDTO list(String id) {
        return createResponseDTO(
                repository.findById(UUID.fromString(id))
                        .orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserResponseDTO getByUsername(String username) {
        return createResponseDTO(
                repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    private UserResponseDTO createResponseDTO(Users user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public ProfileResponseDTO getProfile(UUID userId) {
        Users user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return new ProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    @Transactional
    public ProfileResponseDTO updateProfile(UUID userId, UpdateProfileDTO dto) {
        Users user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update name if provided
        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }

        // Update username if provided and different
        if (dto.username() != null && !dto.username().isBlank() 
            && !dto.username().equals(user.getUsername())) {
            
            repository.findByUsername(dto.username()).ifPresent(existingUser -> {
                throw new IllegalArgumentException("Username unavailable");
            });
            user.setUsername(dto.username());
        }

        // Update email if provided and different
        if (dto.email() != null && !dto.email().isBlank() 
            && !dto.email().equals(user.getEmail())) {
            
            repository.findByEmail(dto.email()).ifPresent(existingUser -> {
                throw new IllegalArgumentException("Email already in use");
            });
            user.setEmail(dto.email());
        }

        // Update password if provided
        if (dto.newPassword() != null && !dto.newPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        }

        user = repository.save(user);

        return new ProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
