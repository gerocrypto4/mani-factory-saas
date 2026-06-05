package com.manifactory.backend.production.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateProductionBatchDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", inclusive = true, message = "Quantity must be greater than zero")
    private BigDecimal quantityKg;

    @NotNull(message = "Batch date is required")
    private LocalDate batchDate;

    private String note;
}
