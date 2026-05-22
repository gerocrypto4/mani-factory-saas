package com.manifactory.backend.products.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateProductDTO {
    private Long tenantId;
    private String name;
    private String description;
    private BigDecimal price;
    private Double weight;
    private String imageUrl;
    private boolean active = true;
}
