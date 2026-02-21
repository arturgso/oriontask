package br.com.oriontask.backend.dharmas.dto;

import jakarta.validation.constraints.NotBlank;

public record NewDharmasDTO(@NotBlank String name, String color) {}
