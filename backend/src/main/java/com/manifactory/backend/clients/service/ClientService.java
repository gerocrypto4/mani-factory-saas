package com.manifactory.backend.clients.service;

import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.ClientPageResponseDTO;
import com.manifactory.backend.clients.dto.ClientOrderHistoryResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.dto.UpdateClientDTO;

import java.util.List;

public interface ClientService {
    ClientResponseDTO create(Long tenantId, CreateClientDTO dto);
    ClientResponseDTO getById(Long tenantId, Long id);
    List<ClientResponseDTO> listByTenant(Long tenantId);
    ClientPageResponseDTO searchByTenant(Long tenantId, String query, int page, int size);
    ClientOrderHistoryResponseDTO getOrderHistory(Long tenantId, Long clientId, int limit);
    ClientResponseDTO update(Long tenantId, Long id, UpdateClientDTO dto);
    void delete(Long tenantId, Long id);
}
