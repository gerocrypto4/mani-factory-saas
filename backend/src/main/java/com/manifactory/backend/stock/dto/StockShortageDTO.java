package com.manifactory.backend.stock.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockShortageDTO {
    private Long productId;
    private String productName;
    private BigDecimal required;
    private BigDecimal available;
}
