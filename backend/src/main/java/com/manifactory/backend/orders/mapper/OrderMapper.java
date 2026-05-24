package com.manifactory.backend.orders.mapper;

import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.CreateOrderItemDTO;
import com.manifactory.backend.orders.dto.OrderItemResponseDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.entity.OrderItem;
import com.manifactory.backend.orders.entity.OrderStatus;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderDTO dto, Long tenantId, Map<Long, BigDecimal> productPriceById) {
        Order order = Order.builder()
                .tenantId(tenantId)
                .clientId(dto.getClientId())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = dto.getItems().stream()
                .map(itemDto -> toItemEntity(itemDto, order, productPriceById.get(itemDto.getProductId())))
                .collect(Collectors.toList());

        order.setItems(items);
        order.setTotal(calculateTotal(items));
        return order;
    }

    private OrderItem toItemEntity(CreateOrderItemDTO dto, Order order, BigDecimal catalogPrice) {
        BigDecimal quantity = BigDecimal.valueOf(dto.getQuantity());
        BigDecimal unitPrice = catalogPrice;
        BigDecimal totalPrice = unitPrice.multiply(quantity);
        return OrderItem.builder()
                .order(order)
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
    }

    public OrderResponseDTO toDto(Order order) {
        if (order == null) {
            return null;
        }
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setTenantId(order.getTenantId());
        dto.setClientId(order.getClientId());
        dto.setStatus(order.getStatus());
        dto.setTotal(order.getTotal());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setItems(order.getItems() == null ? List.of() : order.getItems().stream().map(this::toItemDto).collect(Collectors.toList()));
        return dto;
    }

    private OrderItemResponseDTO toItemDto(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
