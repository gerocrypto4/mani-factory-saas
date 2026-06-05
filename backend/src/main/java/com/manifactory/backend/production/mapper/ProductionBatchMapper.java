package com.manifactory.backend.production.mapper;

import com.manifactory.backend.production.dto.CreateProductionBatchDTO;
import com.manifactory.backend.production.dto.ProductionBatchResponseDTO;
import com.manifactory.backend.production.entity.ProductionBatch;
import org.springframework.stereotype.Component;

@Component
public class ProductionBatchMapper {

    public ProductionBatch toEntity(CreateProductionBatchDTO dto, Long tenantId) {
        return ProductionBatch.builder()
                .tenantId(tenantId)
                .productId(dto.getProductId())
                .quantityKg(dto.getQuantityKg())
                .batchDate(dto.getBatchDate())
                .note(dto.getNote())
                .status("COMPLETED")
                .build();
    }

    public ProductionBatchResponseDTO toResponse(ProductionBatch batch, String productName) {
        return new ProductionBatchResponseDTO(
                batch.getId(),
                batch.getTenantId(),
                batch.getProductId(),
                productName,
                batch.getQuantityKg(),
                batch.getBatchDate(),
                batch.getNote(),
                batch.getStatus(),
                batch.getCreatedAt()
        );
    }
}
