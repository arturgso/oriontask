package br.com.oriontask.backend.service;

import br.com.oriontask.backend.dto.dharmas.DharmasDTO;
import br.com.oriontask.backend.dto.dharmas.NewDharmasDTO;
import br.com.oriontask.backend.dto.dharmas.UpdateDharmasDTO;
import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.mappers.DharmasMapper;
import br.com.oriontask.backend.model.Dharmas;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.DharmasRepository;
import br.com.oriontask.backend.repository.TasksRepository;
import br.com.oriontask.backend.repository.UsersRepository;
import br.com.oriontask.backend.utils.ColorGenerator;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DharmasService {
  private final DharmasRepository repository;
  private final UsersRepository uRepository;
  private final TasksRepository tasksRepository;

  private final DharmasMapper dharmasMapper;

  private static final int MAX_DHARMAS_PER_USER = 8;

  public List<DharmasDTO> getDharmasByUser(String userId, boolean includeHidden) {
    UUID userUuid = UUID.fromString(userId);
    List<Dharmas> dharmaList;
    if (includeHidden) {
      dharmaList = repository.findByUserId(userUuid);
    } else {
      dharmaList = repository.findByUserIdAndHiddenFalse(userUuid);
    }

    return dharmasMapper.toDTO(dharmaList);
  }

  public DharmasDTO create(NewDharmasDTO createDTO, String userId) {
    Users user =
        uRepository
            .findById(UUID.fromString(userId))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Long dharmaCount = repository.countByUser(user);

    if (dharmaCount >= MAX_DHARMAS_PER_USER) {
      throw new IllegalStateException("Maximum number of dharmas reached for this user");
    }

    Dharmas dharmas = dharmasMapper.toEntity(createDTO);
    dharmas.setUser(user);
    String color = dharmas.getColor();

    if (color == null) {
      dharmas.setColor(ColorGenerator.generateRandomColor());
    }

    dharmas = repository.save(dharmas);
    return dharmasMapper.toDTO(dharmas);
  }

  @Transactional
  public DharmasDTO updateDharmas(UpdateDharmasDTO editDTO, Long dharmasId) {
    Dharmas dharmas =
        repository
            .findById(dharmasId)
            .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

    dharmas = dharmasMapper.partialUpdate(editDTO, dharmas);

    dharmas = repository.save(dharmas);
    return dharmasMapper.toDTO(dharmas);
  }

  @Transactional
  public void deleteDharmas(Long dharmasId) {
    Dharmas dharmas =
        repository
            .findById(dharmasId)
            .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

    // Check for active (non-completed) tasks before deleting
    long activeTasksCount =
        tasksRepository.findByDharmasId(dharmasId, Pageable.unpaged()).stream()
            .filter(task -> task.getStatus() != TaskStatus.DONE)
            .count();

    if (activeTasksCount > 0) {
      throw new IllegalStateException(
          "Cannot delete Dharmas with active tasks. Complete or move tasks first.");
    }

    repository.delete(dharmas);
  }

  @Transactional
  public void toggleHidden(Long dharmasId) {
    Dharmas dharmas =
        repository
            .findById(dharmasId)
            .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

    dharmas.setHidden(!dharmas.getHidden());

    // Update all tasks of the dharmas to inherit the hidden state
    var tasks = tasksRepository.findByDharmasId(dharmasId, Pageable.unpaged());
    tasks.forEach(
        task -> {
          task.setHidden(dharmas.getHidden());
        });
    tasksRepository.saveAll(tasks);
  }
}
