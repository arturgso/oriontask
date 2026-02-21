package br.com.oriontask.backend.tasks.exception;

public class TaskDeletionNotAllowedException extends IllegalStateException {
  public TaskDeletionNotAllowedException() {
    super("Completed tasks cannot be deleted (history)");
  }
}
