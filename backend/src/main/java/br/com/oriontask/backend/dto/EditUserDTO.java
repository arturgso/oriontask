package br.com.oriontask.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record EditUserDTO(@NotBlank(message = "Nome é obrigatório") String name,
                @NotBlank(message = "username é obrigatório") String username) {
}
