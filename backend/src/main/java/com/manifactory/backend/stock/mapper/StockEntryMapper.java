package com.manifactory.backend.stock.mapper;

import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.dto.StockEntryResponseDTO;
import com.manifactory.backend.stock.entity.StockEntry;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class StockEntryMapper {

    public StockEntry toEntity(CreateStockEntryDTO dto, Long tenantId) {
        return StockEntry.builder()
                .tenantId(tenantId)
                .productId(dto.getProductId())
                .type(dto.getType())
                .quantity(dto.getQuantity() == null ? BigDecimal.ZERO : dto.getQuantity())
                .note(normalize(dto.getNote()))
                .entryDate(dto.getEntryDate())
                .build();
    }

    public StockEntryResponseDTO toResponse(StockEntry entry, String productName) {
        return new StockEntryResponseDTO(
                entry.getId(),
                entry.getTenantId(),
                entry.getProductId(),
                productName,
                entry.getType(),
                entry.getQuantity(),
                entry.getNote(),
                entry.getEntryDate(),
                entry.getCreatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
