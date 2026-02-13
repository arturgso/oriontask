package br.com.oriontask.backend.dto.dharmas;

import jakarta.validation.constraints.NotBlank;

public record NewDharmasDTO(
        @NotBlank String name,
        String color
) {
}
