package com.manifactory.backend.clients.controller;

import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.dto.UpdateClientDTO;
import com.manifactory.backend.clients.service.ClientService;
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

    // For now tenantId is expected as request param/header; in future extract from JWT

    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@RequestParam Long tenantId, @Valid @RequestBody CreateClientDTO dto) {
        ClientResponseDTO created = service.create(tenantId, dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> list(@RequestParam Long tenantId) {
        return ResponseEntity.ok(service.listByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> get(@RequestParam Long tenantId, @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(tenantId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> update(@RequestParam Long tenantId, @PathVariable Long id, @Valid @RequestBody UpdateClientDTO dto) {
        return ResponseEntity.ok(service.update(tenantId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam Long tenantId, @PathVariable Long id) {
        service.delete(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
