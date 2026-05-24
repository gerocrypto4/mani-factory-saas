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
    private OrderServiceImpl service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(OrderRepository.class);
        mapper = new OrderMapper();
        clientRepository = Mockito.mock(ClientRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        service = new OrderServiceImpl(repository, mapper, clientRepository, productRepository);
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
}
