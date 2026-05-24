package com.manifactory.backend.orders.dto;

import com.manifactory.backend.orders.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusDTO {

    @NotNull
    private OrderStatus status;
}
