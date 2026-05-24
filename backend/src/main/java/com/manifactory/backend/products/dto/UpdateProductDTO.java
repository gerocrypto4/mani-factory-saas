package com.manifactory.backend.products.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateProductDTO {
    @Size(max = 255, message = "Product name must be at most 255 characters")
    private String name;
    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    private Double weight;
    @Size(max = 255, message = "Image URL must be at most 255 characters")
    private String imageUrl;
    private Boolean active;
}
