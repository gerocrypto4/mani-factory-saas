package com.manifactory.backend.finance.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FinanceCategoryTotalDTO {
    private String category;
    private BigDecimal total;
}
