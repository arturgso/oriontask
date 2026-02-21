package br.com.oriontask.backend.exceptions.task;

public class TaskStatusChangeNotAllowedException extends IllegalStateException {
  public TaskStatusChangeNotAllowedException() {
    super("Completed tasks cannot change status");
  }
}
