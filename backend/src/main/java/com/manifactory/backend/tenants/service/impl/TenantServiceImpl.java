package com.manifactory.backend.tenants.service.impl;

import com.manifactory.backend.tenants.dto.CreateTenantDTO;
import com.manifactory.backend.tenants.dto.TenantResponseDTO;
import com.manifactory.backend.tenants.dto.UpdateTenantDTO;
import com.manifactory.backend.tenants.entity.Tenant;
import com.manifactory.backend.tenants.mapper.TenantMapper;
import com.manifactory.backend.tenants.repository.TenantRepository;
import com.manifactory.backend.tenants.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    private final TenantRepository repository;
    private final TenantMapper mapper;

    @Override
    public TenantResponseDTO create(CreateTenantDTO dto) {
        Tenant t = mapper.toEntity(dto);
        Tenant saved = repository.save(t);
        return mapper.toResponse(saved);
    }

    @Override
    public TenantResponseDTO getById(Long id) {
        Tenant t = repository.findById(id).orElseThrow(() -> new RuntimeException("Tenant not found"));
        return mapper.toResponse(t);
    }

    @Override
    public List<TenantResponseDTO> listAll() {
        return repository.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public TenantResponseDTO update(Long id, UpdateTenantDTO dto) {
        Tenant t = repository.findById(id).orElseThrow(() -> new RuntimeException("Tenant not found"));
        t.setName(dto.getName());
        if (dto.getActive() != null) t.setActive(dto.getActive());
        Tenant saved = repository.save(t);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
