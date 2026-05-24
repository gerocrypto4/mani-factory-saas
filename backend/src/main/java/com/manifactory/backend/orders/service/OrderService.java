package com.manifactory.backend.orders.service;

import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.dto.UpdateOrderStatusDTO;
import java.util.List;

public interface OrderService {

    OrderResponseDTO create(Long tenantId, CreateOrderDTO dto);

    OrderResponseDTO getById(Long tenantId, Long id);

    List<OrderResponseDTO> listByTenant(Long tenantId);

    OrderResponseDTO updateStatus(Long tenantId, Long id, UpdateOrderStatusDTO dto);

    void delete(Long tenantId, Long id);
}
