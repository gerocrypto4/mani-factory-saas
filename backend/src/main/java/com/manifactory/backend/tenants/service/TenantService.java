package com.manifactory.backend.tenants.service;

import com.manifactory.backend.tenants.dto.CreateTenantDTO;
import com.manifactory.backend.tenants.dto.TenantResponseDTO;
import com.manifactory.backend.tenants.dto.UpdateTenantDTO;

import java.util.List;

public interface TenantService {
    TenantResponseDTO create(CreateTenantDTO dto);
    TenantResponseDTO getById(Long id);
    List<TenantResponseDTO> listAll();
    TenantResponseDTO update(Long id, UpdateTenantDTO dto);
    void delete(Long id);
}
