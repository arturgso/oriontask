package br.com.oriontask.backend.tasks.controller;

import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.tasks.dto.NewTaskDTO;
import br.com.oriontask.backend.tasks.dto.TaskDTO;
import br.com.oriontask.backend.tasks.dto.UpdateTaskDTO;
import br.com.oriontask.backend.tasks.service.TasksService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

  @GetMapping
  public ResponseEntity<Page<TaskDTO>> list(
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) Long dharmaId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    Page<TaskDTO> tasks =
        tasksService.listTasks(userId, dharmaId, status, PageRequest.of(page, size));
    return ResponseEntity.ok(tasks);
  }

  @PostMapping
  public ResponseEntity<TaskDTO> create(
      @RequestBody @Valid NewTaskDTO createDTO, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.ok(tasksService.create(createDTO, userId));
  }

  @PatchMapping("{taskId}")
  public ResponseEntity<TaskDTO> update(
      @RequestBody @Valid UpdateTaskDTO editDTO,
      @PathVariable Long taskId,
      Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.ok(tasksService.updateTask(editDTO, taskId, userId));
  }

  @PatchMapping("/{taskId}/now")
  public ResponseEntity<TaskDTO> moveToNow(
      @PathVariable Long taskId, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.ok(tasksService.moveToNow(taskId, userId));
  }

  @PatchMapping("/{taskId}/snooze")
  public ResponseEntity<TaskDTO> snoozeTask(
      @PathVariable Long taskId, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.ok(tasksService.snoozeTask(taskId, userId));
  }

  @PatchMapping("/{taskId}/change-status")
  public ResponseEntity<TaskDTO> changeStatus(
      @PathVariable Long taskId, @RequestParam TaskStatus status, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.ok(tasksService.changeStatus(taskId, status, userId));
  }

  @PatchMapping("/{taskId}/done")
  public ResponseEntity<TaskDTO> markAsDone(
      @PathVariable Long taskId, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.ok(tasksService.markAsDone(taskId, userId));
  }

  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long taskId, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    tasksService.deleteTask(taskId, userId);
    return ResponseEntity.noContent().build();
  }
}
