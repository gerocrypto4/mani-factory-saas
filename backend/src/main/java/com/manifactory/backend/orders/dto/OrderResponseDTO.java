package com.manifactory.backend.orders.dto;

import com.manifactory.backend.orders.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long tenantId;
    private Long clientId;
    private OrderStatus status;
    private BigDecimal total;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<OrderItemResponseDTO> items;
}
