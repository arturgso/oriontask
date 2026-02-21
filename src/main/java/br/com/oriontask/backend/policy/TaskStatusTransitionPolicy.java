package br.com.oriontask.backend.policy;

import br.com.oriontask.backend.enums.TaskStatus;
import br.com.oriontask.backend.model.Tasks;
import java.sql.Timestamp;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusTransitionPolicy {

  private static final Long MAX_CURRENT_TASKS = 5L;
  private static final Integer SNOOZE_DURATION_HOURS = 2;

  public void ensureStatusChangeAllowed(Tasks task) {
    if (task.getStatus() == TaskStatus.DONE) {
      throw new IllegalStateException(
          "Completed tasks cannot change status"); // TODO - Create custom exception
    }
  }

  public void ensureNowLimitNotExceeded(Long currentTasksCount, TaskStatus taskStatus) {
    if (currentTasksCount >= MAX_CURRENT_TASKS && taskStatus.equals(TaskStatus.NOW)) {
      throw new IllegalStateException("Maximum of 5 tasks in NOW reached");
    }
  }

  public void applyStatusTransition(Tasks task, TaskStatus newStatus) {
    if (newStatus.equals(TaskStatus.SNOOZED))
      throw new IllegalStateException(
          "To snooze a tasks, use the snoozeTask method from the policy");
    task.setStatus(newStatus);
    task.setSnoozedUntil(null);
  }

  public void snoozeTask(Tasks task) {
    task.setStatus(TaskStatus.SNOOZED);
    task.setSnoozedUntil(getSnoozedUntil());
  }

  private Timestamp getSnoozedUntil() {
    long snoozeMillis = (long) SNOOZE_DURATION_HOURS * 60 * 60 * 1000;
    return new Timestamp(System.currentTimeMillis() + snoozeMillis);
  }
}
