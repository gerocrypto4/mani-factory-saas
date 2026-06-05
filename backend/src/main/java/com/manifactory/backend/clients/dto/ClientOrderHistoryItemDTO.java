package com.manifactory.backend.clients.dto;

import com.manifactory.backend.orders.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientOrderHistoryItemDTO {
    private Long id;
    private OrderStatus status;
    private BigDecimal total;
    private Integer totalItems;
    private Integer totalKg;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
