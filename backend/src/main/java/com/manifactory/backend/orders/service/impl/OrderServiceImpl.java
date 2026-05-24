package com.manifactory.backend.orders.service.impl;

import com.manifactory.backend.exception.NotFoundException;
import com.manifactory.backend.clients.repository.ClientRepository;
import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.dto.UpdateOrderStatusDTO;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.mapper.OrderMapper;
import com.manifactory.backend.orders.repository.OrderRepository;
import com.manifactory.backend.orders.service.OrderService;
import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository repository, OrderMapper mapper, ClientRepository clientRepository,
            ProductRepository productRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    @Override
    public OrderResponseDTO create(Long tenantId, CreateOrderDTO dto) {
        validateClientOwnership(tenantId, dto.getClientId());
        Map<Long, BigDecimal> productPriceById = resolveCatalogPrices(tenantId, dto);
        Order entity = mapper.toEntity(dto, tenantId, productPriceById);
        Order saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getById(Long tenantId, Long id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Order not found"));
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
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setStatus(dto.getStatus());
        Order saved = repository.save(order);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Long tenantId, Long id) {
        Order order = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        repository.delete(order);
    }

    private void validateClientOwnership(Long tenantId, Long clientId) {
        if (clientRepository.findByTenantIdAndId(tenantId, clientId).isEmpty()) {
            throw new IllegalArgumentException("Client does not belong to tenant");
        }
    }

    private Map<Long, BigDecimal> resolveCatalogPrices(Long tenantId, CreateOrderDTO dto) {
        return dto.getItems().stream()
                .map(item -> item.getProductId())
                .distinct()
                .collect(Collectors.toMap(Function.identity(), productId -> {
                    Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                            .orElseThrow(() -> new IllegalArgumentException("Product does not belong to tenant"));
                    return product.getPrice();
                }));
    }
}
