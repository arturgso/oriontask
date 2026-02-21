package br.com.oriontask.backend.users.controller;

import br.com.oriontask.backend.users.dto.UpdateUserDTO;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UsersController {
  private final UsersService service;

  @GetMapping("me")
  public ResponseEntity<UserResponseDTO> list(Authentication authentication) {
    return ResponseEntity.ok().body(service.getMe(authentication));
  }

  @PatchMapping("/me")
  public ResponseEntity<UserResponseDTO> update(
      @Valid @RequestBody UpdateUserDTO dto, Authentication authentication) {
    return ResponseEntity.ok(service.updateProfile(dto, authentication));
  }
}
