package com.manifactory.backend.tenants.service;

import com.manifactory.backend.tenants.dto.CreateTenantDTO;
import com.manifactory.backend.tenants.entity.Tenant;
import com.manifactory.backend.tenants.mapper.TenantMapper;
import com.manifactory.backend.tenants.repository.TenantRepository;
import com.manifactory.backend.tenants.service.impl.TenantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository repository;

    @Mock
    private TenantMapper mapper;

    @InjectMocks
    private TenantServiceImpl service;

    @Test
    void createTenant() {
        CreateTenantDTO dto = new CreateTenantDTO();
        dto.setName("Test Tenant");
        dto.setSlug("test-tenant");

        Tenant entity = Tenant.builder().id(1L).name(dto.getName()).slug(dto.getSlug()).active(true).build();

        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(new com.manifactory.backend.tenants.dto.TenantResponseDTO(1L, "Test Tenant", "test-tenant", true, java.time.LocalDateTime.now()));

        var result = service.create(dto);
        assertNotNull(result);
        assertEquals("Test Tenant", result.getName());
    }

    @Test
    void getByIdNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> service.getById(99L));
        assertTrue(ex.getMessage().contains("Tenant not found"));
    }
}
