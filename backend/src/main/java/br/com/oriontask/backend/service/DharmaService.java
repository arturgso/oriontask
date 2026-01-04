package br.com.oriontask.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.oriontask.backend.dto.EditDharmaDTO;
import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.model.Dharma;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.DharmaRepository;
import br.com.oriontask.backend.repository.TasksRepository;
import br.com.oriontask.backend.repository.UsersRepository;
import br.com.oriontask.backend.utils.ColorGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DharmaService {
   private final DharmaRepository repository; 
   private final UsersRepository   uRepository;
   private final TasksRepository tasksRepository;

    private static final int MAX_DHARMAS_PER_USER = 8;

    public List<Dharma> getDharmasByUser(String userId, boolean includeHidden) {
        UUID userUuid = UUID.fromString(userId);
        if (includeHidden) {
            return repository.findByUserId(userUuid);
        }
        return repository.findByUserIdAndHiddenFalse(userUuid);
    }

    public Dharma create(EditDharmaDTO createDTO, String userId) {
        Users user = uRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Long dharmaCount = repository.countByUser(user);

        if (dharmaCount >= MAX_DHARMAS_PER_USER) {
            throw new IllegalStateException("Maximum number of dharmas reached for this user");
        }

        String color = createDTO.color() != null ? createDTO.color() : ColorGenerator.generateRandomColor();

        Dharma dharma = Dharma.builder()
            .user(user)
            .name(createDTO.name())
            .description(createDTO.description())
            .color(color)
            .hidden(createDTO.hidden() != null ? createDTO.hidden() : false)
            .build();

        dharma = repository.save(dharma);
        return dharma;
    }

    @Transactional
    public Dharma updateDharma(EditDharmaDTO editDTO, Long dharmaId) {
        Dharma dharma = repository.findById(dharmaId)
            .orElseThrow(() -> new IllegalArgumentException("Dharma not found"));

        dharma.setName(editDTO.name());
        dharma.setDescription(editDTO.description());
        dharma.setColor(editDTO.color());
        if (editDTO.hidden() != null) {
            dharma.setHidden(editDTO.hidden());
        }
        dharma.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        dharma = repository.save(dharma);
        return dharma;
    }

    @Transactional
    public void deleteDharma(Long dharmaId) {
        Dharma dharma = repository.findById(dharmaId)
            .orElseThrow(() -> new IllegalArgumentException("Dharma não encontrado"));

        // Verifica se há tasks ativas (não concluídas)
        long activeTasksCount = tasksRepository.findByDharmaId(dharmaId).stream()
            .filter(task -> task.getStatus() != TaskStatus.DONE)
            .count();

        if (activeTasksCount > 0) {
            throw new IllegalStateException("Não é possível deletar Dharma com tasks ativas. Complete ou mova as tasks primeiro.");
        }

    @Transactional
    public Dharma toggleHidden(Long dharmaId) {
        Dharma dharma = repository.findById(dharmaId)
            .orElseThrow(() -> new IllegalArgumentException("Dharma não encontrado"));
        
        dharma.setHidden(!dharma.getHidden());
        dharma.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        
        // Atualizar todas as tasks do dharma para herdar o estado hidden
        var tasks = tasksRepository.findByDharmaId(dharmaId);
        tasks.forEach(task -> {
            task.setHidden(dharma.getHidden());
            task.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        });
        tasksRepository.saveAll(tasks);
        
        return repository.save(dharma);
    }

        repository.delete(dharma);
    }
}
