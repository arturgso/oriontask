package br.com.oriontask.backend.service;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.oriontask.backend.dto.EditTasksDTO;
import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.model.Dharma;
import br.com.oriontask.backend.model.Tasks;
import br.com.oriontask.backend.repository.DharmaRepository;
import br.com.oriontask.backend.repository.TasksRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TasksService {

    private final TasksRepository repository;
    private final DharmaRepository dharmaRepository;

    @org.springframework.beans.factory.annotation.Value("${task.snooze.duration-hours:2}")
    private int snoozeDurationHours;

    private static final int MAX_NOW_TASKS = 5;

    public Tasks create(EditTasksDTO createDTO, Long dharmaId) {
        Dharma dharma = dharmaRepository.findById(dharmaId)
                .orElseThrow(() -> new IllegalArgumentException("Dharma not found"));

        Tasks task = Tasks.builder()
                .dharma(dharma)
                .title(createDTO.title())
                .description(createDTO.description())
                .karmaType(createDTO.karmaType())
                .effortLevel(createDTO.effortLevel())
                .hidden(dharma.getHidden()) // Inherit hidden flag from parent dharma
                .status(TaskStatus.NEXT)
                .build();

        return repository.save(task);
    }

    @Transactional
    public Tasks updateTask(EditTasksDTO editDTO, Long taskId) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Completed tasks cannot be edited");
        }

        task.setTitle(editDTO.title());
        task.setDescription(editDTO.description());
        task.setKarmaType(editDTO.karmaType());
        task.setEffortLevel(editDTO.effortLevel());
        if (editDTO.hidden() != null) {
            task.setHidden(editDTO.hidden());
        }
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return repository.save(task);
    }

    @Transactional
    public Tasks moveToNow(Long taskId) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Completed tasks cannot change status");
        }

        Long nowCount = repository.countByDharmaUserIdAndStatus(
                task.getDharma().getUser().getId(),
                TaskStatus.NOW);

        if (nowCount >= MAX_NOW_TASKS) {
            throw new IllegalStateException("Maximum of 5 tasks in NOW reached");
        }

        task.setStatus(TaskStatus.NOW);
        task.setSnoozedUntil(null);
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return repository.save(task);
    }

    @Transactional
    public Tasks changeStatus(Long taskId, TaskStatus newStatus) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Completed tasks cannot change status");
        }

        if (newStatus == TaskStatus.NOW) {
            Long nowCount = repository.countByDharmaUserIdAndStatus(
                    task.getDharma().getUser().getId(),
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

        return repository.save(task);
    }

    @Transactional
    public Tasks markAsDone(Long taskId) {
        Tasks task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Task is already completed");
        }

        task.setStatus(TaskStatus.DONE);
        task.setSnoozedUntil(null);
        task.setCompletedAt(new Timestamp(System.currentTimeMillis()));
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return repository.save(task);
    }

    public Page<Tasks> getTasksByDharma(Long dharmaId, Pageable pageable) {
        return repository.findByDharmaId(dharmaId, pageable);
    }

    public Page<Tasks> getTasksByStatus(Long dharmaId, TaskStatus status, Pageable pageable) {
        return repository.findByDharmaIdAndStatus(dharmaId, status, pageable);
    }

    public Page<Tasks> getTasksByUserAndStatus(String userId, TaskStatus status, Pageable pageable) {
        return repository.findByDharmaUserIdAndStatus(UUID.fromString(userId), status, pageable);
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
