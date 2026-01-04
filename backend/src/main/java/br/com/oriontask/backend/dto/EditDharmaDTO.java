package br.com.oriontask.backend.dto;

public record EditDharmaDTO(
    String name,
    String description,
    String color,
    Boolean hidden
) {
    
}
