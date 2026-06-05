package com.manifactory.backend.production.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionBatchResponseDTO {
    private Long id;
    private Long tenantId;
    private Long productId;
    private String productName;
    private BigDecimal quantityKg;
    private LocalDate batchDate;
    private String note;
    private String status;
    private OffsetDateTime createdAt;
}
