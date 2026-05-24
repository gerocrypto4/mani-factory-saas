package com.manifactory.backend.tenants.controller;

import com.manifactory.backend.tenants.dto.CreateTenantDTO;
import com.manifactory.backend.tenants.dto.TenantResponseDTO;
import com.manifactory.backend.tenants.dto.UpdateTenantDTO;
import com.manifactory.backend.tenants.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Administracion de tenants")
@SecurityRequirement(name = "bearerAuth")
public class TenantController {
    private final TenantService service;

    @PostMapping
    @Operation(summary = "Crear tenant")
    public ResponseEntity<TenantResponseDTO> create(@Valid @RequestBody CreateTenantDTO dto) {
        TenantResponseDTO created = service.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar tenants")
    public ResponseEntity<List<TenantResponseDTO>> list() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tenant por id")
    public ResponseEntity<TenantResponseDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tenant")
    public ResponseEntity<TenantResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateTenantDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol SUPERADMIN")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
