package com.manifactory.backend.products.controller;

import com.manifactory.backend.products.dto.CreateProductDTO;
import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.dto.UpdateProductDTO;
import com.manifactory.backend.products.service.ProductService;
import com.manifactory.backend.security.TenantResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "CRUD de productos por tenant")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crear producto")
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody CreateProductDTO dto) {
        Long resolvedTenant = TenantResolver.resolve(dto.getTenantId());
        dto.setTenantId(resolvedTenant);
        ProductResponseDTO created = service.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar productos de tenant")
    public ResponseEntity<List<ProductResponseDTO>> list(@RequestParam(required = false) Long tenantId) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        return ResponseEntity.ok(service.listByTenant(resolvedTenant));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por id")
    public ResponseEntity<ProductResponseDTO> get(@RequestParam(required = false) Long tenantId, @PathVariable Long id) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        ProductResponseDTO dto = service.getById(resolvedTenant, id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<ProductResponseDTO> update(@RequestParam(required = false) Long tenantId, @PathVariable Long id,
            @Valid @RequestBody UpdateProductDTO dto) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        ProductResponseDTO updated = service.update(resolvedTenant, id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<Void> delete(@RequestParam(required = false) Long tenantId, @PathVariable Long id) {
        Long resolvedTenant = TenantResolver.resolve(tenantId);
        service.delete(resolvedTenant, id);
        return ResponseEntity.noContent().build();
    }
}
