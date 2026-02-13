package br.com.oriontask.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.oriontask.backend.dto.dharmas.DharmaDTO;
import br.com.oriontask.backend.dto.dharmas.NewDharmaDTO;
import br.com.oriontask.backend.dto.dharmas.UpdateDharmaDTO;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

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
    private final UsersRepository uRepository;
    private final TasksRepository tasksRepository;

    private final DharmaMapper dharmaMapper;

    private static final int MAX_DHARMAS_PER_USER = 8;

    public List<DharmaDTO> getDharmasByUser(String userId, boolean includeHidden) {
        UUID userUuid = UUID.fromString(userId);
        List<Dharma> dharmaList;
        if (includeHidden) {
            dharmaList = repository.findByUserId(userUuid);
        } else {
           dharmaList = repository.findByUserIdAndHiddenFalse(userUuid);
        }

        return  dharmaMapper.toDTO(dharmaList);
    }

    public DharmaDTO create(NewDharmaDTO createDTO, String userId) {
        Users user = uRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long dharmaCount = repository.countByUser(user);

        if (dharmaCount >= MAX_DHARMAS_PER_USER) {
            throw new IllegalStateException("Maximum number of dharmas reached for this user");
        }

        Dharma dharma  = dharmaMapper.toEntity(createDTO);
        dharma.setUser(user);
        String color = dharma.getColor();

        if (color == null) {
            dharma.setColor(ColorGenerator.generateRandomColor());
        }

        dharma = repository.save(dharma);
        return dharmaMapper.toDTO(dharma);
    }

    @Transactional
    public DharmaDTO updateDharma(UpdateDharmaDTO editDTO, Long dharmaId) {
        Dharma dharma = repository.findById(dharmaId)
                .orElseThrow(() -> new IllegalArgumentException("Dharma not found"));

        dharma.setName(editDTO.name());
        dharma.setColor(editDTO.color());

        dharma = repository.save(dharma);
        return dharmaMapper.toDTO(dharma);
    }

    @Transactional
    public void deleteDharma(Long dharmaId) {
        Dharma dharma = repository.findById(dharmaId)
                .orElseThrow(() -> new IllegalArgumentException("Dharma not found"));

        // Check for active (non-completed) tasks before deleting
        long activeTasksCount = tasksRepository.findByDharmaId(dharmaId, Pageable.unpaged()).stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .count();

        if (activeTasksCount > 0) {
            throw new IllegalStateException("Cannot delete Dharma with active tasks. Complete or move tasks first.");
        }

        repository.delete(dharma);
    }

    @Transactional
    public void toggleHidden(Long dharmaId) {
        Dharma dharma = repository.findById(dharmaId)
                .orElseThrow(() -> new IllegalArgumentException("Dharma not found"));

        dharma.setHidden(!dharma.getHidden());

        // Update all tasks of the dharma to inherit the hidden state
        var tasks = tasksRepository.findByDharmaId(dharmaId, Pageable.unpaged());
        tasks.forEach(task -> {
            task.setHidden(dharma.getHidden());
        });
        tasksRepository.saveAll(tasks);
    }
}
