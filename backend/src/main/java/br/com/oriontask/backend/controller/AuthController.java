package br.com.oriontask.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.oriontask.backend.dto.EditUserDTO;
import br.com.oriontask.backend.dto.UserResponseDTO;
import br.com.oriontask.backend.service.UsersService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsersService service;

    @PostMapping("signup")
    public ResponseEntity<UserResponseDTO> signup(@RequestBody @Validated EditUserDTO createDTO) {
        return new ResponseEntity<>(service.create(createDTO), HttpStatus.CREATED);
    }
}
