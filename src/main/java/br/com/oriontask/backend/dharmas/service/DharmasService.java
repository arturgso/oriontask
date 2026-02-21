package br.com.oriontask.backend.dharmas.service;

import br.com.oriontask.backend.dharmas.dto.DharmasDTO;
import br.com.oriontask.backend.dharmas.dto.NewDharmasDTO;
import br.com.oriontask.backend.dharmas.dto.UpdateDharmasDTO;
import br.com.oriontask.backend.dharmas.mapper.DharmasMapper;
import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.dharmas.policy.DharmasPolicy;
import br.com.oriontask.backend.dharmas.repository.DharmasRepository;
import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.shared.utils.ColorGenerator;
import br.com.oriontask.backend.shared.utils.UserLookupService;
import br.com.oriontask.backend.tasks.repository.TasksRepository;
import br.com.oriontask.backend.users.model.Users;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DharmasService {
  private final DharmasRepository repository;
  private final UserLookupService userLookup;
  private final TasksRepository tasksRepository;

  private final DharmasMapper dharmasMapper;
  private final DharmasPolicy dharmasPolicy;

  public DharmasDTO create(NewDharmasDTO createDTO, UUID userId) {
    log.info("DharmasService.create requested userId={}", userId);
    Users user = userLookup.getRequiredUse(userId);

    Long dharmaCount = repository.countByUser(user);

    dharmasPolicy.validateMaxDharmasPerUser(dharmaCount);

    Dharmas dharmas = dharmasMapper.toEntity(createDTO);
    dharmas.setUser(user);
    String color = dharmas.getColor();

    if (color == null) {
      dharmas.setColor(ColorGenerator.generateRandomColor());
    }

    dharmas = repository.save(dharmas);
    log.info("DharmasService.create completed dharmasId={} userId={}", dharmas.getId(), userId);
    return dharmasMapper.toDTO(dharmas);
  }

  public List<DharmasDTO> listDharmas(UUID userId, boolean includeHidden) {
    log.debug(
        "DharmasService.getDharmasByUser requested userId={} includeHidden={}",
        userId,
        includeHidden);
    List<Dharmas> dharmaList;
    if (includeHidden) {
      dharmaList = repository.findByUserId(userId);
    } else {
      dharmaList = repository.findByUserIdAndHiddenFalse(userId);
    }

    List<DharmasDTO> result = dharmasMapper.toDTO(dharmaList);
    log.debug(
        "DharmasService.getDharmasByUser completed userId={} returned={}", userId, result.size());
    return result;
  }

  @Transactional
  public DharmasDTO updateDharmas(UUID userId, UpdateDharmasDTO editDTO, Long dharmasId) {
    log.info("DharmasService.updateDharmas requested dharmasId={}", dharmasId);
    Dharmas dharmas =
        repository
            .findByIdAndUserId(dharmasId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

    dharmas = dharmasMapper.partialUpdate(editDTO, dharmas);

    dharmas = repository.save(dharmas);
    log.info("DharmasService.updateDharmas completed dharmasId={}", dharmasId);
    return dharmasMapper.toDTO(dharmas);
  }

  @Transactional
  public void deleteDharmas(Long dharmasId, UUID userId) {
    log.info("DharmasService.deleteDharmas requested dharmasId={}", dharmasId);
    Dharmas dharmas =
        repository
            .findByIdAndUserId(dharmasId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

    // Check for active (non-completed) tasks before deleting
    long activeTasksCount =
        tasksRepository.findByDharmasId(dharmasId, Pageable.unpaged()).stream()
            .filter(task -> task.getStatus() != TaskStatus.DONE)
            .count();

    if (activeTasksCount > 0) {
      log.warn(
          "DharmasService.deleteDharmas blocked dharmasId={} activeTasksCount={}",
          dharmasId,
          activeTasksCount);
      throw new IllegalStateException(
          "Cannot delete Dharmas with active tasks. Complete or move tasks first.");
    }

    repository.delete(dharmas);
    log.info("DharmasService.deleteDharmas completed dharmasId={}", dharmasId);
  }

  @Transactional
  public void toggleHidden(Long dharmasId, UUID userId) {
    log.info("DharmasService.toggleHidden requested dharmasId={}", dharmasId);
    Dharmas dharmas =
        repository
            .findByIdAndUserId(dharmasId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

    dharmas.setHidden(!dharmas.getHidden());

    // TODO - Move to service to method HideAllTasks
    // Update all tasks of the dharmas to inherit the hidden state
    var tasks = tasksRepository.findByDharmasId(dharmasId, Pageable.unpaged());
    tasks.forEach(
        task -> {
          task.setHidden(dharmas.getHidden());
        });
    tasksRepository.saveAll(tasks);
    log.info(
        "DharmasService.toggleHidden completed dharmasId={} hidden={} tasksUpdated={}",
        dharmasId,
        dharmas.getHidden(),
        tasks.getContent().size());
  }
}
