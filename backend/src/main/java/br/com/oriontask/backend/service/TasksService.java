package br.com.oriontask.backend.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

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

    private static final int MAX_NOW_TASKS = 5;

    public Tasks create(EditTasksDTO createDTO, Long dharmaId) {
        Dharma dharma = dharmaRepository.findById(dharmaId)
            .orElseThrow(() -> new IllegalArgumentException("Dharma não encontrado"));

        Tasks task = Tasks.builder()
            .dharma(dharma)
            .title(createDTO.title())
            .description(createDTO.description())
            .karmaType(createDTO.karmaType())
            .effortLevel(createDTO.effortLevel())
            .status(TaskStatus.NEXT)
            .build();

        return repository.save(task);
    }

    @Transactional
    public Tasks updateTask(EditTasksDTO editDTO, Long taskId) {
        Tasks task = repository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task não encontrada"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Tasks concluídas não podem ser editadas");
        }

        task.setTitle(editDTO.title());
        task.setDescription(editDTO.description());
        task.setKarmaType(editDTO.karmaType());
        task.setEffortLevel(editDTO.effortLevel());
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return repository.save(task);
    }

    @Transactional
    public Tasks moveToNow(Long taskId) {
        Tasks task = repository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task não encontrada"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Tasks concluídas não podem mudar de status");
        }

        Long nowCount = repository.countByDharmaUserIdAndStatus(
            task.getDharma().getUser().getId(), 
            TaskStatus.NOW
        );

        if (nowCount >= MAX_NOW_TASKS) {
            throw new IllegalStateException("Máximo de 5 tasks em NOW atingido");
        }

        task.setStatus(TaskStatus.NOW);
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return repository.save(task);
    }

    @Transactional
    public Tasks changeStatus(Long taskId, TaskStatus newStatus) {
        Tasks task = repository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task não encontrada"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Tasks concluídas não podem mudar de status");
        }

        if (newStatus == TaskStatus.NOW) {
            Long nowCount = repository.countByDharmaUserIdAndStatus(
                task.getDharma().getUser().getId(), 
                TaskStatus.NOW
            );

            if (nowCount >= MAX_NOW_TASKS) {
                throw new IllegalStateException("Máximo de 5 tasks em NOW atingido");
            }
        }

        task.setStatus(newStatus);
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return repository.save(task);
    }

    @Transactional
    public Tasks markAsDone(Long taskId) {
        Tasks task = repository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task não encontrada"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Task já está concluída");
        }

        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(new Timestamp(System.currentTimeMillis()));
        task.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return repository.save(task);
    }

    public List<Tasks> getTasksByDharma(Long dharmaId) {
        return repository.findByDharmaId(dharmaId);
    }

    public List<Tasks> getTasksByStatus(Long dharmaId, TaskStatus status) {
        return repository.findByDharmaIdAndStatus(dharmaId, status);
    }

    public List<Tasks> getTasksByUserAndStatus(String userId, TaskStatus status) {
        return repository.findByDharmaUserIdAndStatus(UUID.fromString(userId), status);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Tasks task = repository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task não encontrada"));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Tasks concluídas não podem ser deletadas (histórico)");
        }

        repository.delete(task);
    }
}
