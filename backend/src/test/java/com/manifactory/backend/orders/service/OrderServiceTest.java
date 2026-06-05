package com.manifactory.backend.orders.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.manifactory.backend.clients.repository.ClientRepository;
import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.CreateOrderItemDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.dto.UpdateOrderStatusDTO;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.entity.OrderItem;
import com.manifactory.backend.orders.entity.OrderStatus;
import com.manifactory.backend.orders.mapper.OrderMapper;
import com.manifactory.backend.orders.repository.OrderRepository;
import com.manifactory.backend.orders.service.impl.OrderServiceImpl;
import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.repository.ProductRepository;
import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.dto.StockLevelDTO;
import com.manifactory.backend.stock.service.StockService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class OrderServiceTest {

    private OrderRepository repository;
    private OrderMapper mapper;
    private ClientRepository clientRepository;
    private ProductRepository productRepository;
    private StockService stockService;
    private OrderServiceImpl service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(OrderRepository.class);
        mapper = new OrderMapper();
        clientRepository = Mockito.mock(ClientRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        stockService = Mockito.mock(StockService.class);
        service = new OrderServiceImpl(repository, mapper, clientRepository, productRepository, stockService);
    }

    @Test
    void createAndGetOrder() {
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setClientId(1L);
        CreateOrderItemDTO item = new CreateOrderItemDTO();
        item.setProductId(2L);
        item.setQuantity(2);
        dto.setItems(List.of(item));

        Order saved = Order.builder()
                .id(5L)
                .tenantId(1L)
                .clientId(1L)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.valueOf(20.00))
                .build();
        OrderItem orderItem = OrderItem.builder()
                .id(3L)
                .order(saved)
                .productId(2L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(10.00))
                .totalPrice(BigDecimal.valueOf(20.00))
                .build();
        saved.setItems(List.of(orderItem));

        Mockito.when(repository.save(ArgumentMatchers.any(Order.class))).thenReturn(saved);
        Mockito.when(repository.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(saved));
        Mockito.when(clientRepository.findByTenantIdAndId(1L, 1L)).thenReturn(Optional.of(new com.manifactory.backend.clients.entity.Client()));
        Product product = Product.builder().id(2L).tenantId(1L).price(BigDecimal.valueOf(10.00)).name("P").build();
        Mockito.when(productRepository.findByIdAndTenantId(2L, 1L)).thenReturn(Optional.of(product));

        OrderResponseDTO created = service.create(1L, dto);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(5L);
        assertThat(created.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(20.00));

        OrderResponseDTO fetched = service.getById(1L, 5L);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getItems()).hasSize(1);
        assertThat(fetched.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void updateOrderStatus() {
        Order existing = Order.builder().id(7L).tenantId(1L).clientId(1L).status(OrderStatus.PENDING).total(BigDecimal.valueOf(15.00)).build();
        Mockito.when(repository.findByIdAndTenantId(7L, 1L)).thenReturn(Optional.of(existing));
        Mockito.when(repository.save(ArgumentMatchers.any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderStatusDTO update = new UpdateOrderStatusDTO();
        update.setStatus(OrderStatus.CONFIRMED);

        OrderResponseDTO result = service.updateStatus(1L, 7L, update);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatusToConfirmed_createsStockWithdrawals() {
        Order existing = Order.builder()
                .id(8L).tenantId(1L).clientId(1L)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.valueOf(300.00)).build();
        OrderItem item1 = OrderItem.builder()
                .id(1L).order(existing).productId(10L).quantity(100)
                .unitPrice(BigDecimal.ONE).totalPrice(BigDecimal.valueOf(100)).build();
        OrderItem item2 = OrderItem.builder()
                .id(2L).order(existing).productId(11L).quantity(50)
                .unitPrice(BigDecimal.ONE).totalPrice(BigDecimal.valueOf(50)).build();
        existing.setItems(java.util.List.of(item1, item2));

        Mockito.when(repository.findByIdAndTenantId(8L, 1L)).thenReturn(Optional.of(existing));
        Mockito.when(repository.save(ArgumentMatchers.any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(stockService.currentLevels(1L)).thenReturn(List.of(
                new StockLevelDTO(10L, "P1", BigDecimal.valueOf(200), null),
                new StockLevelDTO(11L, "P2", BigDecimal.valueOf(100), null)
        ));

        UpdateOrderStatusDTO update = new UpdateOrderStatusDTO();
        update.setStatus(OrderStatus.CONFIRMED);

        service.updateStatus(1L, 8L, update);

        Mockito.verify(stockService, Mockito.times(2))
                .create(ArgumentMatchers.eq(1L), ArgumentMatchers.any(CreateStockEntryDTO.class));
    }
}
