package com.manifactory.backend.finance.mapper;

import com.manifactory.backend.finance.dto.CreateFinanceEntryDTO;
import com.manifactory.backend.finance.dto.FinanceEntryResponseDTO;
import com.manifactory.backend.finance.entity.FinanceEntry;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FinanceEntryMapper {

    public FinanceEntry toEntity(CreateFinanceEntryDTO dto, Long tenantId) {
        return FinanceEntry.builder()
                .tenantId(tenantId)
                .type(dto.getType())
                .category(normalize(dto.getCategory()))
                .note(normalize(dto.getNote()))
                .amount(dto.getAmount() == null ? BigDecimal.ZERO : dto.getAmount())
                .entryDate(dto.getEntryDate())
                .build();
    }

    public FinanceEntryResponseDTO toResponse(FinanceEntry entity) {
        return new FinanceEntryResponseDTO(
                entity.getId(),
                entity.getTenantId(),
                entity.getType(),
                entity.getCategory(),
                entity.getNote(),
                entity.getAmount(),
                entity.getEntryDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
