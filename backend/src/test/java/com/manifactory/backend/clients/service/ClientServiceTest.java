package com.manifactory.backend.clients.service;

import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.ClientPageResponseDTO;
import com.manifactory.backend.clients.dto.ClientOrderHistoryResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.dto.UpdateClientDTO;
import com.manifactory.backend.clients.entity.Client;
import com.manifactory.backend.clients.mapper.ClientMapper;
import com.manifactory.backend.clients.repository.ClientRepository;
import com.manifactory.backend.clients.service.impl.ClientServiceImpl;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.entity.OrderItem;
import com.manifactory.backend.orders.entity.OrderStatus;
import com.manifactory.backend.orders.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientMapper mapper;

    @InjectMocks
    private ClientServiceImpl service;

    @Test
    void createClient() {
        CreateClientDTO dto = new CreateClientDTO();
        dto.setName("Cliente A");

        Client entity = Client.builder().id(1L).tenantId(1L).name(dto.getName()).build();

        when(mapper.toEntity(any(), any())).thenReturn(entity);
        when(repository.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(new ClientResponseDTO(1L,1L,"Cliente A",null,null,null,null,null, LocalDateTime.now(), LocalDateTime.now()));

        ClientResponseDTO res = service.create(1L, dto);
        assertNotNull(res);
        assertEquals("Cliente A", res.getName());
    }

    @Test
    void createClientRejectsDuplicatePhone() {
        CreateClientDTO dto = new CreateClientDTO();
        dto.setName("Cliente B");
        dto.setPhone("3000");

        when(repository.findFirstByTenantIdAndPhone(1L, "3000"))
                .thenReturn(Optional.of(Client.builder().id(2L).tenantId(1L).phone("3000").build()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));
        assertTrue(ex.getMessage().contains("Phone already exists"));
    }

    @Test
    void updateClientRejectsDuplicateEmail() {
        Client existing = Client.builder().id(1L).tenantId(1L).name("Cliente A").build();
        when(repository.findByTenantIdAndId(1L, 1L)).thenReturn(Optional.of(existing));
        when(repository.findFirstByTenantIdAndEmailIgnoreCase(1L, "cliente@mail.com"))
                .thenReturn(Optional.of(Client.builder().id(2L).tenantId(1L).email("cliente@mail.com").build()));

        UpdateClientDTO dto = new UpdateClientDTO();
        dto.setName("Cliente A");
        dto.setEmail("cliente@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(1L, 1L, dto));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    void searchClientsReturnsPagedResult() {
        Client client = Client.builder().id(3L).tenantId(1L).name("Cliente C").phone("3000").build();
        when(repository.searchByTenantId(1L, "cliente", PageRequest.of(0, 6, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "updatedAt"))))
                .thenReturn(new PageImpl<>(List.of(client), PageRequest.of(0, 6, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "updatedAt")), 1));
        when(mapper.toResponse(any())).thenReturn(new ClientResponseDTO(3L, 1L, "Cliente C", null, null, "3000", null, null, LocalDateTime.now(), LocalDateTime.now()));

        ClientPageResponseDTO res = service.searchByTenant(1L, " cliente ", 0, 6);
        assertNotNull(res);
        assertEquals(1L, res.getTotalElements());
        assertEquals(1, res.getContent().size());
        assertEquals("Cliente C", res.getContent().get(0).getName());
        assertEquals("cliente", res.getQuery());
    }

    @Test
    void getByIdNotFound() {
        when(repository.findByTenantIdAndId(1L, 99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> service.getById(1L, 99L));
        assertTrue(ex.getMessage().contains("Client not found"));
    }

    @Test
    void getOrderHistoryReturnsRecentOrders() {
        Client client = Client.builder().id(7L).tenantId(1L).name("Cliente Historia").build();
        Order first = Order.builder().id(20L).tenantId(1L).clientId(7L).status(OrderStatus.CONFIRMED).total(java.math.BigDecimal.valueOf(6000))
                .items(List.of(
                        OrderItem.builder().id(1L).productId(101L).quantity(10).unitPrice(java.math.BigDecimal.valueOf(300)).totalPrice(java.math.BigDecimal.valueOf(3000)).build(),
                        OrderItem.builder().id(2L).productId(102L).quantity(20).unitPrice(java.math.BigDecimal.valueOf(150)).totalPrice(java.math.BigDecimal.valueOf(3000)).build()
                ))
                .build();
        Order second = Order.builder().id(19L).tenantId(1L).clientId(7L).status(OrderStatus.PENDING).total(java.math.BigDecimal.valueOf(3000))
                .items(List.of(
                        OrderItem.builder().id(3L).productId(103L).quantity(10).unitPrice(java.math.BigDecimal.valueOf(300)).totalPrice(java.math.BigDecimal.valueOf(3000)).build()
                ))
                .build();
        var pageable = PageRequest.of(0, 3, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        when(repository.findByTenantIdAndId(1L, 7L)).thenReturn(Optional.of(client));
        when(orderRepository.findByTenantIdAndClientId(1L, 7L, pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));

        ClientOrderHistoryResponseDTO history = service.getOrderHistory(1L, 7L, 3);

        assertNotNull(history);
        assertEquals(7L, history.getClientId());
        assertEquals("Cliente Historia", history.getClientName());
        assertEquals(2L, history.getTotalOrders());
        assertNotNull(history.getLatestOrder());
        assertEquals(20L, history.getLatestOrder().getId());
        assertEquals(2, history.getLatestOrder().getTotalItems());
        assertEquals(30, history.getLatestOrder().getTotalKg());
        assertEquals(2, history.getRecentOrders().size());
    }
}
