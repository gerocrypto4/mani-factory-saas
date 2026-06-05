package com.manifactory.backend.orders.service.impl;

import com.manifactory.backend.clients.repository.ClientRepository;
import com.manifactory.backend.exception.InsufficientStockException;
import com.manifactory.backend.exception.NotFoundException;
import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.dto.UpdateOrderStatusDTO;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.entity.OrderItem;
import com.manifactory.backend.orders.entity.OrderStatus;
import com.manifactory.backend.orders.mapper.OrderMapper;
import com.manifactory.backend.orders.repository.OrderRepository;
import com.manifactory.backend.orders.service.OrderService;
import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.repository.ProductRepository;
import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.dto.StockLevelDTO;
import com.manifactory.backend.stock.dto.StockShortageDTO;
import com.manifactory.backend.stock.entity.StockEntryType;
import com.manifactory.backend.stock.service.StockService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final StockService stockService;

    public OrderServiceImpl(OrderRepository repository, OrderMapper mapper, ClientRepository clientRepository,
            ProductRepository productRepository, StockService stockService) {
        this.repository = repository;
        this.mapper = mapper;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
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
        OrderStatus previousStatus = order.getStatus();
        OrderStatus newStatus = dto.getStatus();
        order.setStatus(newStatus);
        Order saved = repository.save(order);
        if (newStatus == OrderStatus.CONFIRMED && previousStatus != OrderStatus.CONFIRMED) {
            List<StockShortageDTO> shortages = validateStockForOrder(tenantId, saved);
            if (!shortages.isEmpty()) {
                throw new InsufficientStockException(shortages);
            }
            deductStockForOrder(tenantId, saved);
        }
        return mapper.toDto(saved);
    }

    private List<StockShortageDTO> validateStockForOrder(Long tenantId, Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) return List.of();

        Map<Long, BigDecimal> availableByProduct = stockService.currentLevels(tenantId).stream()
                .collect(Collectors.toMap(StockLevelDTO::getProductId, StockLevelDTO::getCurrentQuantityKg));

        Map<Long, BigDecimal> requiredByProduct = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            requiredByProduct.merge(item.getProductId(), BigDecimal.valueOf(item.getQuantity()), BigDecimal::add);
        }

        List<StockShortageDTO> shortages = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : requiredByProduct.entrySet()) {
            Long productId = entry.getKey();
            BigDecimal required = entry.getValue();
            BigDecimal available = availableByProduct.getOrDefault(productId, BigDecimal.ZERO);
            if (required.compareTo(available) > 0) {
                String productName = productRepository.findByIdAndTenantId(productId, tenantId)
                        .map(Product::getName)
                        .orElse("Producto #" + productId);
                shortages.add(new StockShortageDTO(productId, productName, required, available));
            }
        }
        return shortages;
    }

    private void deductStockForOrder(Long tenantId, Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) return;
        for (OrderItem item : order.getItems()) {
            CreateStockEntryDTO dto = new CreateStockEntryDTO();
            dto.setType(StockEntryType.WITHDRAWAL);
            dto.setProductId(item.getProductId());
            dto.setQuantity(BigDecimal.valueOf(item.getQuantity()));
            dto.setNote("Pedido #" + order.getId());
            dto.setEntryDate(LocalDate.now());
            stockService.create(tenantId, dto);
        }
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
