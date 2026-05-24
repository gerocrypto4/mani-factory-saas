package com.manifactory.backend.clients.controller;

import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.dto.UpdateClientDTO;
import com.manifactory.backend.clients.service.ClientService;
import com.manifactory.backend.security.TenantResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService service;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@RequestParam(required = false) Long tenantId, @Valid @RequestBody CreateClientDTO dto) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        ClientResponseDTO created = service.create(resolvedTenant, dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> list(@RequestParam(required = false) Long tenantId) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.listByTenant(resolvedTenant));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> get(@RequestParam(required = false) Long tenantId, @PathVariable Long id) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.getById(resolvedTenant, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> update(@RequestParam(required = false) Long tenantId, @PathVariable Long id, @Valid @RequestBody UpdateClientDTO dto) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.update(resolvedTenant, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam(required = false) Long tenantId, @PathVariable Long id) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        service.delete(resolvedTenant, id);
        return ResponseEntity.noContent().build();
    }
}
