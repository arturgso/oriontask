package br.com.oriontask.backend.controller;

import java.util.List;

import br.com.oriontask.backend.dto.dharmas.DharmasDTO;
import br.com.oriontask.backend.dto.dharmas.NewDharmasDTO;
import br.com.oriontask.backend.dto.dharmas.UpdateDharmasDTO;
import jakarta.validation.Valid;
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

import br.com.oriontask.backend.service.DharmasService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dharmas")
@RequiredArgsConstructor
public class DharmasController {
   
    private final DharmasService dharmasService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DharmasDTO>> getDharmasByUser(
            @PathVariable String userId,
            @RequestParam(required = false, defaultValue = "false") boolean includeHidden) {
        return ResponseEntity.ok(dharmasService.getDharmasByUser(userId, includeHidden));
    }

    @PostMapping("/{userId}/create")
    public ResponseEntity<DharmasDTO> createDharmas(@RequestBody @Valid NewDharmasDTO createDTO, @PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dharmasService.create(
                createDTO, userId));
    }

    @PatchMapping("/edit/{dharmasId}")
    public ResponseEntity<DharmasDTO> editDharmas(@RequestBody @Valid UpdateDharmasDTO editDTO, @PathVariable Long dharmasId) {
        return ResponseEntity.ok(dharmasService.updateDharmas(editDTO, dharmasId));
    }

    @DeleteMapping("/{dharmasId}")
    public ResponseEntity<Void> deleteDharmas(@PathVariable Long dharmasId) {
        dharmasService.deleteDharmas(dharmasId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{dharmasId}/toggle-hidden")
    public ResponseEntity<Void> toggleHidden(@PathVariable Long dharmasId) {
        dharmasService.toggleHidden(dharmasId);
        return ResponseEntity.noContent().build();

    }
}
