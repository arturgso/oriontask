package br.com.oriontask.backend.exceptions.task;

public class TaskAlreadyCompletedException extends IllegalStateException {
  public TaskAlreadyCompletedException() {
    super("Task is already completed");
  }
}
