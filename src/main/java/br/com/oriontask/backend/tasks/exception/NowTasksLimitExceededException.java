package br.com.oriontask.backend.tasks.exception;

public class NowTasksLimitExceededException extends IllegalStateException {
  public NowTasksLimitExceededException() {
    super("Maximum of 5 tasks in NOW reached");
  }
}
