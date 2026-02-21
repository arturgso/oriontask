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

  @PostMapping("/{userId}/create")
  public ResponseEntity<DharmasDTO> createDharmas(
      @RequestBody @Valid NewDharmasDTO createDTO, @PathVariable UUID userId) {
    return ResponseEntity.status(HttpStatus.CREATED).body(dharmasService.create(createDTO, userId));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<DharmasDTO>> getDharmasByUser(
      @PathVariable String userId,
      @RequestParam(required = false, defaultValue = "false") boolean includeHidden) {
    return ResponseEntity.ok(dharmasService.getDharmasByUser(userId, includeHidden));
  }

  @PatchMapping("/edit/{dharmasId}")
  public ResponseEntity<DharmasDTO> editDharmas(
      @RequestBody @Valid UpdateDharmasDTO editDTO, @PathVariable Long dharmasId) {
    return ResponseEntity.ok(dharmasService.updateDharmas(editDTO, dharmasId));
  }

  @PatchMapping("/{dharmasId}/toggle-hidden")
  public ResponseEntity<Void> toggleHidden(@PathVariable Long dharmasId) {
    dharmasService.toggleHidden(dharmasId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{dharmasId}")
  public ResponseEntity<Void> deleteDharmas(@PathVariable Long dharmasId) {
    dharmasService.deleteDharmas(dharmasId);
    return ResponseEntity.noContent().build();
  }
}
