package com.manifactory.backend.auth.controller;

import com.manifactory.backend.auth.dto.CreateUserDTO;
import com.manifactory.backend.auth.dto.UpdatePasswordDTO;
import com.manifactory.backend.auth.dto.UpdateUserDTO;
import com.manifactory.backend.auth.dto.UserResponseDTO;
import com.manifactory.backend.auth.service.AppUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody CreateUserDTO dto) {
        return ResponseEntity.status(201).body(appUserService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> list(@RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(appUserService.list(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO dto) {
        return ResponseEntity.ok(appUserService.update(id, dto));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<UserResponseDTO> updatePassword(@PathVariable Long id, @Valid @RequestBody UpdatePasswordDTO dto) {
        return ResponseEntity.ok(appUserService.updatePassword(id, dto));
    }
}
