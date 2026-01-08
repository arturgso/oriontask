package br.com.oriontask.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.oriontask.backend.dto.ProfileResponseDTO;
import br.com.oriontask.backend.dto.UpdateProfileDTO;
import br.com.oriontask.backend.dto.UserResponseDTO;
import br.com.oriontask.backend.service.UsersService;
import br.com.oriontask.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService service;

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> list(@PathVariable String id) {
        return new ResponseEntity<>(service.list(id), HttpStatus.OK);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getByUsername(@PathVariable String username) {
        return new ResponseEntity<>(service.getByUsername(username), HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile() {
        return ResponseEntity.ok(service.getProfile(SecurityUtils.getCurrentUserId()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        return ResponseEntity.ok(service.updateProfile(SecurityUtils.getCurrentUserId(), dto));
    }
}
