package com.manifactory.backend.products.controller;

import com.manifactory.backend.products.dto.CreateProductDTO;
import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.dto.UpdateProductDTO;
import com.manifactory.backend.products.service.ProductService;
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
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody CreateProductDTO dto) {
        ProductResponseDTO created = service.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> list(@RequestParam Long tenantId) {
        return ResponseEntity.ok(service.listByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> get(@RequestParam Long tenantId, @PathVariable Long id) {
        ProductResponseDTO dto = service.getById(tenantId, id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(@RequestParam Long tenantId, @PathVariable Long id,
            @RequestBody UpdateProductDTO dto) {
        ProductResponseDTO updated = service.update(tenantId, id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam Long tenantId, @PathVariable Long id) {
        service.delete(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
