package com.manifactory.backend.clients.service.impl;

import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.ClientPageResponseDTO;
import com.manifactory.backend.clients.dto.ClientOrderHistoryItemDTO;
import com.manifactory.backend.clients.dto.ClientOrderHistoryResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.dto.UpdateClientDTO;
import com.manifactory.backend.clients.entity.Client;
import com.manifactory.backend.clients.mapper.ClientMapper;
import com.manifactory.backend.clients.repository.ClientRepository;
import com.manifactory.backend.clients.service.ClientService;
import com.manifactory.backend.exception.NotFoundException;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.entity.OrderItem;
import com.manifactory.backend.orders.repository.OrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository repository;
    private final OrderRepository orderRepository;
    private final ClientMapper mapper;

    @Override
    public ClientResponseDTO create(Long tenantId, CreateClientDTO dto) {
        String normalizedPhone = normalize(dto.getPhone());
        String normalizedEmail = normalize(dto.getEmail());
        validateUniqueContactInfo(tenantId, null, normalizedPhone, normalizedEmail);
        Client entity = mapper.toEntity(dto, tenantId);
        normalizeClient(entity);
        Client saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public ClientResponseDTO getById(Long tenantId, Long id) {
        Client c = repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        return mapper.toResponse(c);
    }

    @Override
    public List<ClientResponseDTO> listByTenant(Long tenantId) {
        return repository.findByTenantId(tenantId).stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ClientPageResponseDTO searchByTenant(Long tenantId, String query, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 50));
        String normalizedQuery = normalize(query);
        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        var pageResult = repository.searchByTenantId(tenantId, normalizedQuery, pageable);
        return new ClientPageResponseDTO(
                pageResult.getContent().stream().map(mapper::toResponse).collect(Collectors.toList()),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.hasNext(),
                pageResult.hasPrevious(),
                normalizedQuery
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ClientOrderHistoryResponseDTO getOrderHistory(Long tenantId, Long clientId, int limit) {
        Client client = repository.findByTenantIdAndId(tenantId, clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        int safeLimit = Math.max(1, Math.min(limit, 10));
        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        var orderPage = orderRepository.findByTenantIdAndClientId(tenantId, clientId, pageable);
        var recentOrders = orderPage.getContent().stream().map(this::toHistoryItem).toList();
        return new ClientOrderHistoryResponseDTO(
                client.getId(),
                client.getName(),
                orderPage.getTotalElements(),
                recentOrders.isEmpty() ? null : recentOrders.get(0),
                recentOrders
        );
    }

    @Override
    public ClientResponseDTO update(Long tenantId, Long id, UpdateClientDTO dto) {
        Client c = repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        c.setName(normalize(dto.getName()));
        c.setBusinessName(normalize(dto.getBusinessName()));
        c.setEmail(normalize(dto.getEmail()));
        c.setPhone(normalize(dto.getPhone()));
        c.setCity(normalize(dto.getCity()));
        c.setPreferredTransport(normalize(dto.getPreferredTransport()));
        validateUniqueContactInfo(tenantId, id, c.getPhone(), c.getEmail());
        Client saved = repository.save(c);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long tenantId, Long id) {
        Client c = repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        repository.delete(c);
    }

    private void validateUniqueContactInfo(Long tenantId, Long currentClientId, String phone, String email) {
        String normalizedPhone = normalize(phone);
        if (normalizedPhone != null) {
            findByTenantIdAndPhone(tenantId, normalizedPhone)
                    .filter(existing -> currentClientId == null || !existing.getId().equals(currentClientId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Phone already exists for this tenant");
                    });
        }

        String normalizedEmail = normalize(email);
        if (normalizedEmail != null) {
            findByTenantIdAndEmail(tenantId, normalizedEmail)
                    .filter(existing -> currentClientId == null || !existing.getId().equals(currentClientId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Email already exists for this tenant");
                    });
        }
    }

    private Optional<Client> findByTenantIdAndPhone(Long tenantId, String phone) {
        return repository.findFirstByTenantIdAndPhone(tenantId, phone);
    }

    private Optional<Client> findByTenantIdAndEmail(Long tenantId, String email) {
        return repository.findFirstByTenantIdAndEmailIgnoreCase(tenantId, email);
    }

    private void normalizeClient(Client client) {
        client.setName(normalize(client.getName()));
        client.setBusinessName(normalize(client.getBusinessName()));
        client.setEmail(normalize(client.getEmail()));
        client.setPhone(normalize(client.getPhone()));
        client.setCity(normalize(client.getCity()));
        client.setPreferredTransport(normalize(client.getPreferredTransport()));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ClientOrderHistoryItemDTO toHistoryItem(Order order) {
        int totalItems = order.getItems() == null ? 0 : order.getItems().size();
        int totalKg = order.getItems() == null
                ? 0
                : order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        return new ClientOrderHistoryItemDTO(
                order.getId(),
                order.getStatus(),
                order.getTotal(),
                totalItems,
                totalKg,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
