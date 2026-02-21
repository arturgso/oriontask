package br.com.oriontask.backend.exceptions.task;

public class InvalidSnoozedStatusTransitionException extends IllegalStateException {
  public InvalidSnoozedStatusTransitionException() {
    super("To snooze a task, use the snoozeTask method from the policy");
  }
}
