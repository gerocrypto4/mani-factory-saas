package com.manifactory.backend.finance.controller;

import com.manifactory.backend.finance.dto.CreateFinanceEntryDTO;
import com.manifactory.backend.finance.dto.FinanceEntryResponseDTO;
import com.manifactory.backend.finance.dto.FinanceSummaryDTO;
import com.manifactory.backend.finance.service.FinanceService;
import com.manifactory.backend.security.TenantResolver;
import jakarta.validation.Valid;
import java.time.YearMonth;
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
@RequestMapping("/api/v1/finances")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService service;

    @PostMapping
    public ResponseEntity<FinanceEntryResponseDTO> create(@RequestParam(required = false) Long tenantId,
            @Valid @RequestBody CreateFinanceEntryDTO dto) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        FinanceEntryResponseDTO created = service.create(resolvedTenant, dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public ResponseEntity<List<FinanceEntryResponseDTO>> list(@RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.listByTenant(resolvedTenant, resolvePeriod(month, year)));
    }

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryDTO> summary(@RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.summary(resolvedTenant, resolvePeriod(month, year)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam(required = false) Long tenantId, @PathVariable Long id) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        service.delete(resolvedTenant, id);
        return ResponseEntity.noContent().build();
    }

    private YearMonth resolvePeriod(Integer month, Integer year) {
        if (month == null && year == null) {
            return YearMonth.now();
        }
        if (month == null || year == null) {
            throw new IllegalArgumentException("Month and year must be provided together");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return YearMonth.of(year, month);
    }
}
