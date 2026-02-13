package br.com.oriontask.backend.dto.tasks;

import br.com.oriontask.backend.enums.EffortLevel;
import br.com.oriontask.backend.enums.KarmaType;
import br.com.oriontask.backend.enums.TaskStatus;
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
