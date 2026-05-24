package com.manifactory.backend.orders.controller;

import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.dto.UpdateOrderStatusDTO;
import com.manifactory.backend.orders.service.OrderService;
import com.manifactory.backend.security.TenantContextHolder;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestParam(required = false) Long tenantId,
            @Valid @RequestBody CreateOrderDTO dto) {
        if (tenantId == null) {
            tenantId = TenantContextHolder.getTenantId();
        }
        OrderResponseDTO created = service.create(tenantId, dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> list(@RequestParam(required = false) Long tenantId) {
        if (tenantId == null) {
            tenantId = TenantContextHolder.getTenantId();
        }
        return ResponseEntity.ok(service.listByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> get(@RequestParam(required = false) Long tenantId,
            @PathVariable Long id) {
        if (tenantId == null) {
            tenantId = TenantContextHolder.getTenantId();
        }
        OrderResponseDTO dto = service.getById(tenantId, id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@RequestParam(required = false) Long tenantId,
            @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusDTO dto) {
        if (tenantId == null) {
            tenantId = TenantContextHolder.getTenantId();
        }
        return ResponseEntity.ok(service.updateStatus(tenantId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam(required = false) Long tenantId, @PathVariable Long id) {
        if (tenantId == null) {
            tenantId = TenantContextHolder.getTenantId();
        }
        service.delete(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
