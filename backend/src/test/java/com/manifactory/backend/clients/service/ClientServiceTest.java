package com.manifactory.backend.clients.service;

import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.entity.Client;
import com.manifactory.backend.clients.mapper.ClientMapper;
import com.manifactory.backend.clients.repository.ClientRepository;
import com.manifactory.backend.clients.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository repository;

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
    void getByIdNotFound() {
        when(repository.findByTenantIdAndId(1L, 99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> service.getById(1L, 99L));
        assertTrue(ex.getMessage().contains("Client not found"));
    }
}
