package br.com.oriontask.backend.dharmas.dto;

import java.sql.Timestamp;

public record DharmasDTO(
    Long id, String name, String color, Boolean hidden, Timestamp createdAt, Timestamp updatedAt) {
  public record BaseUserDTO(String username, String email) {}
}
