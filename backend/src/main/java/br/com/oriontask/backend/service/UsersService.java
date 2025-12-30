package br.com.oriontask.backend.service;

import org.springframework.stereotype.Service;

import br.com.oriontask.backend.dto.EditUserDTO;
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

    @Transactional
    public UserResponseDTO create(EditUserDTO createDTO) {
        repository.findByUsername(createDTO.username()).ifPresent(user -> {
            throw new IllegalArgumentException("Username indisponível");
        });

        Users user = Users.builder()
                .name(createDTO.name())
                .username(createDTO.username())
                .build();

        user = repository.save(user);

        return createResponseDTO(user);
    }

    private UserResponseDTO createResponseDTO(Users user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
