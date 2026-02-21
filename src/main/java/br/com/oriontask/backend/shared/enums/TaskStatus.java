package br.com.oriontask.backend.shared.enums;

public enum TaskStatus {
  NOW,
  @Deprecated
  NEXT,
  WAITING,
  SNOOZED,
  DONE
}
