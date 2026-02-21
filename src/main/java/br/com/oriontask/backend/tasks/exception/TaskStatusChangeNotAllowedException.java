package br.com.oriontask.backend.tasks.exception;

public class TaskStatusChangeNotAllowedException extends IllegalStateException {
  public TaskStatusChangeNotAllowedException() {
    super("Completed tasks cannot change status");
  }
}
