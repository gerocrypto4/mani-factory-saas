package com.manifactory.backend.products.service.impl;

import com.manifactory.backend.products.dto.CreateProductDTO;
import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.dto.UpdateProductDTO;
import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.mapper.ProductMapper;
import com.manifactory.backend.products.repository.ProductRepository;
import com.manifactory.backend.products.service.ProductService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ProductResponseDTO create(CreateProductDTO dto) {
        Product entity = mapper.toEntity(dto);
        Product saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getById(Long tenantId, Long id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listByTenant(Long tenantId) {
        return repository.findAllByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO update(Long tenantId, Long id, UpdateProductDTO dto) {
        Product product = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        mapper.updateEntity(product, dto);
        Product saved = repository.save(product);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Long tenantId, Long id) {
        Product product = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        repository.delete(product);
    }
}
