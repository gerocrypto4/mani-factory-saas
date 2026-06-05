package com.manifactory.backend.stock.dto;

import com.manifactory.backend.stock.entity.StockEntryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockEntryResponseDTO {
    private Long id;
    private Long tenantId;
    private Long productId;
    private String productName;
    private StockEntryType type;
    private BigDecimal quantity;
    private String note;
    private LocalDate entryDate;
    private OffsetDateTime createdAt;
}
