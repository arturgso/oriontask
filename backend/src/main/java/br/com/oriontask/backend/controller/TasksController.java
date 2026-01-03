package br.com.oriontask.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.oriontask.backend.dto.EditTasksDTO;
import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.model.Tasks;
import br.com.oriontask.backend.service.TasksService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TasksController {
    
    private final TasksService tasksService;

    @PostMapping("/{dharmaId}/create")
    public ResponseEntity<Tasks> createTask(@RequestBody EditTasksDTO createDTO, @PathVariable Long dharmaId) {
        Tasks task = tasksService.create(createDTO, dharmaId);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/edit/{taskId}")
    public ResponseEntity<Tasks> editTask(@RequestBody EditTasksDTO editDTO, @PathVariable Long taskId) {
        Tasks updatedTask = tasksService.updateTask(editDTO, taskId);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{taskId}/move-to-now")
    public ResponseEntity<Tasks> moveToNow(@PathVariable Long taskId) {
        Tasks task = tasksService.moveToNow(taskId);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{taskId}/change-status")
    public ResponseEntity<Tasks> changeStatus(
        @PathVariable Long taskId,
        @RequestParam TaskStatus status
    ) {
        Tasks task = tasksService.changeStatus(taskId, status);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{taskId}/mark-done")
    public ResponseEntity<Tasks> markAsDone(@PathVariable Long taskId) {
        Tasks task = tasksService.markAsDone(taskId);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/dharma/{dharmaId}")
    public ResponseEntity<List<Tasks>> getTasksByDharma(@PathVariable Long dharmaId) {
        List<Tasks> tasks = tasksService.getTasksByDharma(dharmaId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/dharma/{dharmaId}/status/{status}")
    public ResponseEntity<List<Tasks>> getTasksByDharmaAndStatus(
        @PathVariable Long dharmaId,
        @PathVariable TaskStatus status
    ) {
        List<Tasks> tasks = tasksService.getTasksByStatus(dharmaId, status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Tasks>> getTasksByUserAndStatus(
        @PathVariable String userId,
        @PathVariable TaskStatus status
    ) {
        List<Tasks> tasks = tasksService.getTasksByUserAndStatus(userId, status);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        tasksService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
