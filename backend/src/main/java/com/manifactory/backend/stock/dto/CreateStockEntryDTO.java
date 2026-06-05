package com.manifactory.backend.stock.dto;

import com.manifactory.backend.stock.entity.StockEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateStockEntryDTO {

    @NotNull(message = "Type is required")
    private StockEntryType type;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", inclusive = true, message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private String note;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;
}
