package com.manifactory.backend.publicapi.dto;

import com.manifactory.backend.orders.entity.OrderStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PublicOrderResponseDTO {
    private Long orderId;
    private OrderStatus status;
    private BigDecimal total;
    private String message;
}
