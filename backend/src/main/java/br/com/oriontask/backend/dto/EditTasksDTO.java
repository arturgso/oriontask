package br.com.oriontask.backend.dto;

import br.com.oriontask.backend.enums.EffortLevel;
import br.com.oriontask.backend.enums.KarmaType;

public record EditTasksDTO(
    String title,
    String description,
    KarmaType karmaType,
    EffortLevel effortLevel
) {
    
}
