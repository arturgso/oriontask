package br.com.oriontask.backend.controller;

import br.com.oriontask.backend.dto.users.UpdateUserDTO;
import br.com.oriontask.backend.dto.users.UserResponseDTO;
import br.com.oriontask.backend.service.UsersService;
import jakarta.validation.Valid;
import java.nio.file.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UsersController {
  private final UsersService service;

  @GetMapping("me")
  public ResponseEntity<UserResponseDTO> getMe(Authentication authentication) {
    return ResponseEntity.ok().body(service.getMe(authentication));
  }

  @GetMapping("{username}")
  public ResponseEntity<UserResponseDTO> list(
      @PathVariable String username, Authentication authentication) throws AccessDeniedException {
    return new ResponseEntity<>(service.list(username, authentication), HttpStatus.OK);
  }

  @PatchMapping("/profile/{username}")
  public ResponseEntity<UserResponseDTO> updateProfile(
      @Valid @RequestBody UpdateUserDTO dto,
      @PathVariable String username,
      Authentication authentication)
      throws AccessDeniedException {
    return ResponseEntity.ok(service.updateProfile(username, dto, authentication));
  }
}
