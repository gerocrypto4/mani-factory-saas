package com.manifactory.backend.finance.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FinanceSummaryDTO {
    private BigDecimal systemSales;
    private long salesOrdersCount;
    private BigDecimal manualIncome;
    private BigDecimal expenses;
    private BigDecimal totalIncome;
    private BigDecimal netResult;
    private List<FinanceCategoryTotalDTO> expenseByCategory;
}
