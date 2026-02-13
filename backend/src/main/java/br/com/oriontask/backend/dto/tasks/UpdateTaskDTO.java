package br.com.oriontask.backend.dto.tasks;

import br.com.oriontask.backend.enums.EffortLevel;
import br.com.oriontask.backend.enums.KarmaType;
import jakarta.validation.constraints.Size;

public record UpdateTaskDTO(
        @Size(min = 5, max = 60) String title,
        @Size(max = 200) String description,
        KarmaType karmaType,
        EffortLevel effortLevel,
        Boolean hidden
) {
}
