package com.manifactory.backend.production.service;

import com.manifactory.backend.production.dto.CreateProductionBatchDTO;
import com.manifactory.backend.production.dto.ProductionBatchResponseDTO;
import java.util.List;

public interface ProductionBatchService {
    ProductionBatchResponseDTO create(Long tenantId, CreateProductionBatchDTO dto);
    List<ProductionBatchResponseDTO> listByTenant(Long tenantId);
    void delete(Long tenantId, Long id);
}
