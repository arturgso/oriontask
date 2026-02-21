package br.com.oriontask.backend.policy;

import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.exceptions.task.InvalidSnoozedStatusTransitionException;
import br.com.oriontask.backend.exceptions.task.NowTasksLimitExceededException;
import br.com.oriontask.backend.exceptions.task.TaskAlreadyCompletedException;
import br.com.oriontask.backend.exceptions.task.TaskDeletionNotAllowedException;
import br.com.oriontask.backend.exceptions.task.TaskStatusChangeNotAllowedException;
import br.com.oriontask.backend.model.Tasks;
import java.sql.Timestamp;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusTransitionPolicy {

  private static final Long MAX_CURRENT_TASKS = 5L;
  private static final Integer SNOOZE_DURATION_HOURS = 2;

  public void ensureStatusChangeAllowed(Tasks task) {
    ensureStatusChangeAllowed(task, false);
  }

  public void ensureStatusChangeAllowed(Tasks task, boolean isDeleteOperation) {
    if (task.getStatus() == TaskStatus.DONE) {
      if (isDeleteOperation) {
        throw new TaskDeletionNotAllowedException();
      }
      throw new TaskStatusChangeNotAllowedException();
    }
  }

  public void ensureNowLimitNotExceeded(Long currentTasksCount, TaskStatus taskStatus) {
    if (taskStatus == null) taskStatus = TaskStatus.NOW;

    if (currentTasksCount >= MAX_CURRENT_TASKS && taskStatus.equals(TaskStatus.NOW)) {
      throw new NowTasksLimitExceededException();
    }
  }

  public void applyStatusTransition(Tasks task, TaskStatus newStatus) {
    if (newStatus.equals(TaskStatus.SNOOZED)) {
      throw new InvalidSnoozedStatusTransitionException();
    }
    task.setStatus(newStatus);
    task.setSnoozedUntil(null);
  }

  public void snoozeTask(Tasks task) {
    task.setStatus(TaskStatus.SNOOZED);
    task.setSnoozedUntil(getSnoozedUntil());
  }

  public void markAsDone(Tasks task) {
    if (task.getStatus() == TaskStatus.DONE) {
      throw new TaskAlreadyCompletedException();
    }

    task.setStatus(TaskStatus.DONE);
    clearSnooze(task);
    task.setCompletedAt(new Timestamp(System.currentTimeMillis()));
  }

  public void markAsNow(Tasks task) {
    if (task.getStatus() == TaskStatus.DONE) {
      throw new TaskAlreadyCompletedException();
    }

    task.setStatus(TaskStatus.NOW);
    clearSnooze(task);
    task.setCompletedAt(new Timestamp(System.currentTimeMillis()));
  }

  public void clearSnooze(Tasks task) {
    task.setSnoozedUntil(null);
  }

  private Timestamp getSnoozedUntil() {
    long snoozeMillis = (long) SNOOZE_DURATION_HOURS * 60 * 60 * 1000;
    return new Timestamp(System.currentTimeMillis() + snoozeMillis);
  }
}
