package br.com.oriontask.backend.dharmas.controller;

import br.com.oriontask.backend.dharmas.dto.DharmasDTO;
import br.com.oriontask.backend.dharmas.dto.NewDharmasDTO;
import br.com.oriontask.backend.dharmas.dto.UpdateDharmasDTO;
import br.com.oriontask.backend.dharmas.service.DharmasService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dharmas")
@RequiredArgsConstructor
public class DharmasController {

  private final DharmasService dharmasService;

  @PostMapping
  public ResponseEntity<DharmasDTO> createDharmas(
      @RequestBody @Valid NewDharmasDTO createDTO, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(dharmasService.create(createDTO, userId));
  }

  @GetMapping
  public ResponseEntity<List<DharmasDTO>> findAll(
      @RequestParam(required = false, defaultValue = "false") boolean includeHidden,
      Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.ok(dharmasService.listDharmas(userId, includeHidden));
  }

  @PatchMapping("{dharmasId}")
  public ResponseEntity<DharmasDTO> update(
      @RequestBody @Valid UpdateDharmasDTO editDTO,
      @PathVariable Long dharmasId,
      Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    return ResponseEntity.ok(dharmasService.updateDharmas(userId, editDTO, dharmasId));
  }

  @PatchMapping("/{dharmasId}/hidden")
  public ResponseEntity<Void> toggleHidden(
      @PathVariable Long dharmasId, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    dharmasService.toggleHidden(dharmasId, userId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{dharmasId}")
  public ResponseEntity<Void> deleteDharmas(
      @PathVariable Long dharmasId, Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    dharmasService.deleteDharmas(dharmasId, userId);
    return ResponseEntity.noContent().build();
  }
}
