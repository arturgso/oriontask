package br.com.oriontask.backend.service;

import java.sql.Timestamp;
import java.util.UUID;

import br.com.oriontask.backend.dto.tasks.NewTaskDTO;
import br.com.oriontask.backend.dto.tasks.TaskDTO;
import br.com.oriontask.backend.dto.tasks.UpdateTaskDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.mappers.TasksMapper;
import br.com.oriontask.backend.model.Dharmas;
import br.com.oriontask.backend.model.Tasks;
import br.com.oriontask.backend.repository.DharmasRepository;
import br.com.oriontask.backend.repository.TasksRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TasksService {

    private final TasksRepository repository;
    private final DharmasRepository dharmasRepository;
    private final TasksMapper tasksMapper;

    @org.springframework.beans.factory.annotation.Value("${task.snooze.duration-hours:2}")
    private int snoozeDurationHours;

    private static final int MAX_NOW_TASKS = 5;

    public TaskDTO create(NewTaskDTO createDTO, Long dharmasId) {
        Dharmas dharmas = dharmasRepository.findById(dharmasId)
                .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));

        Tasks task = tasksMapper.toEntity(createDTO);
        task.setDharmas(dharmas);
        task.setHidden(dharmas.getHidden()); // Inherit hidden flag from parent dharmas
        task.setStatus(TaskStatus.NEXT);

        return tasksMapper.toDTO(repository.save(task));
    }

    @Transactional
    public TaskDTO updateTask(UpdateTaskDTO editDTO, Long taskId) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Completed tasks cannot be edited");
        }

        task = tasksMapper.partialUpdate(editDTO, task);
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return tasksMapper.toDTO(repository.save(task));
    }

    @Transactional
    public TaskDTO moveToNow(Long taskId) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Completed tasks cannot change status");
        }

        Long nowCount = repository.countByDharmasUserIdAndStatus(
                task.getDharmas().getUser().getId(),
                TaskStatus.NOW);

        if (nowCount >= MAX_NOW_TASKS) {
            throw new IllegalStateException("Maximum of 5 tasks in NOW reached");
        }

        task.setStatus(TaskStatus.NOW);
        task.setSnoozedUntil(null);
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return tasksMapper.toDTO(repository.save(task));
    }

    @Transactional
    public TaskDTO changeStatus(Long taskId, TaskStatus newStatus) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Completed tasks cannot change status");
        }

        if (newStatus == TaskStatus.NOW) {
            Long nowCount = repository.countByDharmasUserIdAndStatus(
                    task.getDharmas().getUser().getId(),
                    TaskStatus.NOW);

            if (nowCount >= MAX_NOW_TASKS) {
                throw new IllegalStateException("Maximum of 5 tasks in NOW reached");
            }
        }

        task.setStatus(newStatus);

        if (newStatus == TaskStatus.NEXT) {
            long snoozeMillis = (long) snoozeDurationHours * 60 * 60 * 1000;
            task.setSnoozedUntil(new Timestamp(System.currentTimeMillis() + snoozeMillis));
        } else {
            task.setSnoozedUntil(null);
        }

        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return tasksMapper.toDTO(repository.save(task));
    }

    @Transactional
    public TaskDTO markAsDone(Long taskId) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Task is already completed");
        }

        task.setStatus(TaskStatus.DONE);
        task.setSnoozedUntil(null);
        task.setCompletedAt(new Timestamp(System.currentTimeMillis()));
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return tasksMapper.toDTO(repository.save(task));
    }

    public Page<TaskDTO> getTasksByDharmas(Long dharmasId, Pageable pageable) {
        return repository.findByDharmasId(dharmasId, pageable).map(tasksMapper::toDTO);
    }

    public Page<TaskDTO> getTasksByDharmasAndStatus(Long dharmasId, TaskStatus status, Pageable pageable) {
        return repository.findByDharmasIdAndStatus(dharmasId, status, pageable).map(tasksMapper::toDTO);
    }

    public Page<TaskDTO> getTasksByUserAndStatus(String userId, TaskStatus status, Pageable pageable) {
        return repository.findByDharmasUserIdAndStatus(UUID.fromString(userId), status, pageable).map(tasksMapper::toDTO);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Completed tasks cannot be deleted (history)");
        }

        repository.delete(task);
    }
}
