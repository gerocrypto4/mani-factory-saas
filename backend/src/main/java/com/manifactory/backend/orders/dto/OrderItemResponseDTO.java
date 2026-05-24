package com.manifactory.backend.orders.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItemResponseDTO {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
