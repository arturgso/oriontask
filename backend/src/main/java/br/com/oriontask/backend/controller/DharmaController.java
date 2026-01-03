package br.com.oriontask.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.oriontask.backend.dto.EditDharmaDTO;
import br.com.oriontask.backend.model.Dharma;
import br.com.oriontask.backend.service.DharmaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dharma")
@RequiredArgsConstructor
public class DharmaController {
   
    private final DharmaService dharmaService;

    @PostMapping("/{userId}/create")
    public ResponseEntity<Dharma> createDharma(@RequestBody EditDharmaDTO createDTO, @PathVariable String userId) {
        Dharma dharma = dharmaService.create(createDTO, userId);
        return ResponseEntity.ok(dharma);
    }

    @PatchMapping("/edit/{dharmaId}")
    public ResponseEntity<Dharma> editDharma(@RequestBody EditDharmaDTO editDTO, @PathVariable Long dharmaId) {
        Dharma updatedDharma = dharmaService.updateDharma(editDTO, dharmaId);
        return ResponseEntity.ok(updatedDharma);
    }
}
