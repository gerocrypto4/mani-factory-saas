package com.manifactory.backend.orders.service.impl;

import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.dto.UpdateOrderStatusDTO;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.entity.OrderStatus;
import com.manifactory.backend.orders.mapper.OrderMapper;
import com.manifactory.backend.orders.repository.OrderRepository;
import com.manifactory.backend.orders.service.OrderService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;

    public OrderServiceImpl(OrderRepository repository, OrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public OrderResponseDTO create(Long tenantId, CreateOrderDTO dto) {
        Order entity = mapper.toEntity(dto, tenantId);
        Order saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getById(Long tenantId, Long id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> listByTenant(Long tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateStatus(Long tenantId, Long id, UpdateOrderStatusDTO dto) {
        Order order = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(dto.getStatus());
        Order saved = repository.save(order);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Long tenantId, Long id) {
        Order order = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        repository.delete(order);
    }
}
