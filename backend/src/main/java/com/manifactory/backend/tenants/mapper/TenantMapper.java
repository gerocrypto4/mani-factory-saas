package com.manifactory.backend.tenants.mapper;

import com.manifactory.backend.tenants.dto.CreateTenantDTO;
import com.manifactory.backend.tenants.dto.TenantResponseDTO;
import com.manifactory.backend.tenants.entity.Tenant;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TenantMapper {
    public Tenant toEntity(CreateTenantDTO dto) {
        return Tenant.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .active(dto.getActive() == null ? Boolean.TRUE : dto.getActive())
                .build();
    }

    public TenantResponseDTO toResponse(Tenant entity) {
        return new TenantResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getActive(),
                LocalDateTime.now()
        );
    }
}
