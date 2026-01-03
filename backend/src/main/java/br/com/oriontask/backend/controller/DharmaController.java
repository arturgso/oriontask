package br.com.oriontask.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Dharma>> getDharmasByUser(@PathVariable String userId) {
        List<Dharma> dharmas = dharmaService.getDharmasByUser(userId);
        return ResponseEntity.ok(dharmas);
    }

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

    @DeleteMapping("/{dharmaId}")
    public ResponseEntity<Void> deleteDharma(@PathVariable Long dharmaId) {
        dharmaService.deleteDharma(dharmaId);
        return ResponseEntity.noContent().build();
    }
}
