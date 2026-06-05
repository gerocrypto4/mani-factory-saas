package com.manifactory.backend.stock.controller;

import com.manifactory.backend.security.TenantResolver;
import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.dto.StockEntryResponseDTO;
import com.manifactory.backend.stock.dto.StockLevelDTO;
import com.manifactory.backend.stock.service.StockService;
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
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @PostMapping
    public ResponseEntity<StockEntryResponseDTO> create(@RequestParam(required = false) Long tenantId,
            @Valid @RequestBody CreateStockEntryDTO dto) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        StockEntryResponseDTO created = service.create(resolvedTenant, dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public ResponseEntity<List<StockEntryResponseDTO>> list(@RequestParam(required = false) Long tenantId) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.listByTenant(resolvedTenant));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<StockLevelDTO>> summary(@RequestParam(required = false) Long tenantId) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.currentLevels(resolvedTenant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam(required = false) Long tenantId, @PathVariable Long id) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        service.delete(resolvedTenant, id);
        return ResponseEntity.noContent().build();
    }
}
