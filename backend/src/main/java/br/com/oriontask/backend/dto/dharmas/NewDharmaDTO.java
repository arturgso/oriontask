package br.com.oriontask.backend.dto.dharmas;

import jakarta.validation.constraints.NotBlank;

public record NewDharmaDTO(
        @NotBlank String name,
        String color
) {
}
