package br.com.oriontask.backend.exceptions.task;

public class TaskDeletionNotAllowedException extends IllegalStateException {
  public TaskDeletionNotAllowedException() {
    super("Completed tasks cannot be deleted (history)");
  }
}
