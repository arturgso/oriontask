package br.com.oriontask.backend.tasks.controller;

import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.tasks.dto.NewTaskDTO;
import br.com.oriontask.backend.tasks.dto.TaskDTO;
import br.com.oriontask.backend.tasks.dto.UpdateTaskDTO;
import br.com.oriontask.backend.tasks.service.TasksService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TasksController {

  private final TasksService tasksService;

  @PostMapping("/{dharmasId}/create")
  public ResponseEntity<TaskDTO> createTask(
      @RequestBody @Valid NewTaskDTO createDTO, @PathVariable Long dharmasId) {
    return ResponseEntity.ok(tasksService.create(createDTO, dharmasId));
  }

  @PatchMapping("/edit/{taskId}")
  public ResponseEntity<TaskDTO> editTask(
      @RequestBody @Valid UpdateTaskDTO editDTO, @PathVariable Long taskId) {
    return ResponseEntity.ok(tasksService.updateTask(editDTO, taskId));
  }

  @PatchMapping("/{taskId}/move-to-now")
  public ResponseEntity<TaskDTO> moveToNow(@PathVariable Long taskId) {
    return ResponseEntity.ok(tasksService.moveToNow(taskId));
  }

  @PatchMapping("/{taskId}/change-status")
  public ResponseEntity<TaskDTO> changeStatus(
      @PathVariable Long taskId, @RequestParam TaskStatus status) {
    return ResponseEntity.ok(tasksService.changeStatus(taskId, status));
  }

  @PatchMapping("/{taskId}/mark-done")
  public ResponseEntity<TaskDTO> markAsDone(@PathVariable Long taskId) {
    return ResponseEntity.ok(tasksService.markAsDone(taskId));
  }

  @GetMapping("/dharmas/{dharmasId}")
  public ResponseEntity<Page<TaskDTO>> getTasksByDharmas(
      @PathVariable Long dharmasId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<TaskDTO> tasks =
        tasksService.getTasksByDharmas(
            dharmasId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/dharmas/{dharmasId}/status/{status}")
  public ResponseEntity<Page<TaskDTO>> getTasksByDharmasAndStatus(
      @PathVariable Long dharmasId,
      @PathVariable TaskStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<TaskDTO> tasks =
        tasksService.getTasksByDharmasAndStatus(
            dharmasId, status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/user/{userId}/status/{status}")
  public ResponseEntity<Page<TaskDTO>> getTasksByUserAndStatus(
      @PathVariable String userId,
      @PathVariable TaskStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<TaskDTO> tasks =
        tasksService.getTasksByUserAndStatus(
            userId, status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<Page<TaskDTO>> getTasksByUser(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<TaskDTO> tasks =
        tasksService.getTasksByUser(
            userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    return ResponseEntity.ok(tasks);
  }

  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
    tasksService.deleteTask(taskId);
    return ResponseEntity.noContent().build();
  }
}
