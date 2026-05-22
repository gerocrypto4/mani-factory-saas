package com.manifactory.backend.products.service;

import com.manifactory.backend.products.dto.CreateProductDTO;
import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.dto.UpdateProductDTO;
import java.util.List;

public interface ProductService {

    ProductResponseDTO create(CreateProductDTO dto);

    ProductResponseDTO getById(Long tenantId, Long id);

    List<ProductResponseDTO> listByTenant(Long tenantId);

    ProductResponseDTO update(Long tenantId, Long id, UpdateProductDTO dto);

    void delete(Long tenantId, Long id);
}
