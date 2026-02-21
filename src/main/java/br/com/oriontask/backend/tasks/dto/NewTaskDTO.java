package br.com.oriontask.backend.tasks.dto;

import br.com.oriontask.backend.shared.enums.EffortLevel;
import br.com.oriontask.backend.shared.enums.KarmaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewTaskDTO(
    @NotBlank @Size(min = 5, max = 60) String title,
    @Size(max = 200) String description,
    KarmaType karmaType,
    EffortLevel effortLevel) {}
