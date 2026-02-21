package br.com.oriontask.backend.tasks.exception;

public class TaskAlreadyCompletedException extends IllegalStateException {
  public TaskAlreadyCompletedException() {
    super("Task is already completed");
  }
}
