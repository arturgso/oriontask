package br.com.oriontask.backend.tasks.dto;

import br.com.oriontask.backend.shared.enums.EffortLevel;
import br.com.oriontask.backend.shared.enums.KarmaType;
import br.com.oriontask.backend.shared.enums.TaskStatus;
import java.sql.Timestamp;

public record TaskDTO(
    Long id,
    Long dharmasId,
    String title,
    String description,
    KarmaType karmaType,
    EffortLevel effortLevel,
    TaskStatus status,
    Boolean hidden,
    Timestamp completedAt,
    Timestamp snoozedUntil,
    Timestamp createdAt,
    Timestamp updatedAt) {}
