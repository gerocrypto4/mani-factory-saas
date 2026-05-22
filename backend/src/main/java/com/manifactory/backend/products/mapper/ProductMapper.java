package com.manifactory.backend.products.mapper;

import com.manifactory.backend.products.dto.CreateProductDTO;
import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.dto.UpdateProductDTO;
import com.manifactory.backend.products.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductDTO dto) {
        if (dto == null) return null;
        return Product.builder()
                .tenantId(dto.getTenantId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .weight(dto.getWeight())
                .imageUrl(dto.getImageUrl())
                .active(dto.isActive())
                .build();
    }

    public void updateEntity(Product product, UpdateProductDTO dto) {
        if (dto == null || product == null) return;
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getWeight() != null) product.setWeight(dto.getWeight());
        if (dto.getImageUrl() != null) product.setImageUrl(dto.getImageUrl());
        if (dto.getActive() != null) product.setActive(dto.getActive());
    }

    public ProductResponseDTO toDto(Product product) {
        if (product == null) return null;
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setTenantId(product.getTenantId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setWeight(product.getWeight());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.isActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}
