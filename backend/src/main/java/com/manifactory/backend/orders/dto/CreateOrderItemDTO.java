package com.manifactory.backend.orders.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateOrderItemDTO {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
