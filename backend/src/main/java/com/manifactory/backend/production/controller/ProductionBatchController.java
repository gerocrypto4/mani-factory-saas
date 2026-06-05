package com.manifactory.backend.production.controller;

import com.manifactory.backend.production.dto.CreateProductionBatchDTO;
import com.manifactory.backend.production.dto.ProductionBatchResponseDTO;
import com.manifactory.backend.production.service.ProductionBatchService;
import com.manifactory.backend.security.TenantResolver;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/production")
@RequiredArgsConstructor
public class ProductionBatchController {

    private final ProductionBatchService service;

    @PostMapping
    public ResponseEntity<ProductionBatchResponseDTO> create(@RequestParam(required = false) Long tenantId,
            @Valid @RequestBody CreateProductionBatchDTO dto) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.status(201).body(service.create(resolvedTenant, dto));
    }

    @GetMapping
    public ResponseEntity<List<ProductionBatchResponseDTO>> list(@RequestParam(required = false) Long tenantId) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.listByTenant(resolvedTenant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam(required = false) Long tenantId, @PathVariable Long id) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        service.delete(resolvedTenant, id);
        return ResponseEntity.noContent().build();
    }
}
