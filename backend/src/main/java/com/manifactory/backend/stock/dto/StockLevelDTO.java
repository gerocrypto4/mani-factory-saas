package com.manifactory.backend.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockLevelDTO {
    private Long productId;
    private String productName;
    private BigDecimal currentQuantityKg;
    private LocalDate lastMovementDate;
}
