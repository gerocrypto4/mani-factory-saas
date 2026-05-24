package com.manifactory.backend.clients.mapper;

import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.entity.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    public Client toEntity(CreateClientDTO dto, Long tenantId) {
        return Client.builder()
                .tenantId(tenantId)
                .name(dto.getName())
                .businessName(dto.getBusinessName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .city(dto.getCity())
                .preferredTransport(dto.getPreferredTransport())
                .build();
    }

    public ClientResponseDTO toResponse(Client entity) {
        return new ClientResponseDTO(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getBusinessName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCity(),
                entity.getPreferredTransport(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
