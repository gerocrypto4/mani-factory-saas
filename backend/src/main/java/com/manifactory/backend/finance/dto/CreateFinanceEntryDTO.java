package com.manifactory.backend.finance.dto;

import com.manifactory.backend.finance.entity.FinanceEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateFinanceEntryDTO {

    @NotNull(message = "Type is required")
    private FinanceEntryType type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Category is required")
    private String category;

    private String note;

    @NotNull(message = "Entry date is required")
    @PastOrPresent(message = "Entry date cannot be in the future")
    private LocalDate entryDate;
}
