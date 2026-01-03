package br.com.oriontask.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.oriontask.backend.dto.EditDharmaDTO;
import br.com.oriontask.backend.model.Dharma;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.DharmaRepository;
import br.com.oriontask.backend.repository.UsersRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DharmaService {
   private final DharmaRepository repository; 
   private final UsersRepository   uRepository;

    private static final int MAX_DHARMAS_PER_USER = 8;

    public Dharma create(EditDharmaDTO createDTO, String userId) {
        Users user = uRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Long dharmaCount = repository.countByUser(user);

        if (dharmaCount >= MAX_DHARMAS_PER_USER) {
            throw new IllegalStateException("Maximum number of dharmas reached for this user");
        }

        Dharma dharma = Dharma.builder()
            .user(user)
            .name(createDTO.name())
            .description(createDTO.description())
            .color(createDTO.color())
            .build();

        dharma = repository.save(dharma);
        return dharma;
    }
}
