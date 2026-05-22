package com.manifactory.backend.clients.service.impl;

import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.dto.UpdateClientDTO;
import com.manifactory.backend.clients.entity.Client;
import com.manifactory.backend.clients.mapper.ClientMapper;
import com.manifactory.backend.clients.repository.ClientRepository;
import com.manifactory.backend.clients.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository repository;
    private final ClientMapper mapper;

    @Override
    public ClientResponseDTO create(Long tenantId, CreateClientDTO dto) {
        Client entity = mapper.toEntity(dto, tenantId);
        Client saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public ClientResponseDTO getById(Long tenantId, Long id) {
        Client c = repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return mapper.toResponse(c);
    }

    @Override
    public List<ClientResponseDTO> listByTenant(Long tenantId) {
        return repository.findByTenantId(tenantId).stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ClientResponseDTO update(Long tenantId, Long id, UpdateClientDTO dto) {
        Client c = repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        c.setName(dto.getName());
        c.setBusinessName(dto.getBusinessName());
        c.setEmail(dto.getEmail());
        c.setPhone(dto.getPhone());
        c.setCity(dto.getCity());
        c.setPreferredTransport(dto.getPreferredTransport());
        Client saved = repository.save(c);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long tenantId, Long id) {
        Client c = repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        repository.delete(c);
    }
}
