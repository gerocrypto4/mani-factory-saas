package com.manifactory.backend.publicapi.controller;

import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.service.ProductService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/catalog")
public class PublicCatalogController {

    private final ProductService productService;

    public PublicCatalogController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> listProducts(@RequestParam Long tenantId) {
        List<ProductResponseDTO> products = productService.listByTenant(tenantId).stream()
                .filter(ProductResponseDTO::isActive)
                .toList();
        return ResponseEntity.ok(products);
    }
}
