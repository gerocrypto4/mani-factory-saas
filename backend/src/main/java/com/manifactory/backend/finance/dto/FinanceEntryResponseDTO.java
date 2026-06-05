package com.manifactory.backend.finance.dto;

import com.manifactory.backend.finance.entity.FinanceEntryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FinanceEntryResponseDTO {
    private Long id;
    private Long tenantId;
    private FinanceEntryType type;
    private String category;
    private String note;
    private BigDecimal amount;
    private LocalDate entryDate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
