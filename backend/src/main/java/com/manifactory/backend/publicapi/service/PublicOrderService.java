package com.manifactory.backend.publicapi.service;

import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.entity.Client;
import com.manifactory.backend.clients.repository.ClientRepository;
import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.CreateOrderItemDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.service.OrderService;
import com.manifactory.backend.orders.rules.OrderRulesProperties;
import com.manifactory.backend.publicapi.dto.PublicOrderRequestDTO;
import com.manifactory.backend.publicapi.dto.PublicOrderResponseDTO;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PublicOrderService {

    private final ClientRepository clientRepository;
    private final OrderService orderService;
    private final OrderRulesProperties orderRulesProperties;

    public PublicOrderService(ClientRepository clientRepository, OrderService orderService,
            OrderRulesProperties orderRulesProperties) {
        this.clientRepository = clientRepository;
        this.orderService = orderService;
        this.orderRulesProperties = orderRulesProperties;
    }

    @Transactional
    public PublicOrderResponseDTO createOrder(Long tenantId, PublicOrderRequestDTO request) {
        OrderRulesProperties.TenantRules rules = orderRulesProperties.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("No order rules configured for tenant"));
        validateBusinessRules(request, rules);

        Long clientId = resolveOrCreateClient(tenantId, request);

        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setClientId(clientId);
        createOrderDTO.setItems(toOrderItems(request));

        OrderResponseDTO order = orderService.create(tenantId, createOrderDTO);
        return new PublicOrderResponseDTO(order.getId(), order.getStatus(), order.getTotal(),
                "Pedido recibido correctamente");
    }

    private void validateBusinessRules(PublicOrderRequestDTO request, OrderRulesProperties.TenantRules rules) {
        Map<Long, Integer> qtyByProduct = request.getItems().stream()
                .collect(Collectors.toMap(
                        PublicOrderRequestDTO.Item::getProductId,
                        PublicOrderRequestDTO.Item::getQuantity,
                        Integer::sum,
                        LinkedHashMap::new));

        int totalKg = qtyByProduct.values().stream().mapToInt(Integer::intValue).sum();
        if (totalKg < rules.getMinOrderKg()) {
            int remainingKg = rules.getMinOrderKg() - totalKg;
            throw new IllegalArgumentException(
                    "El pedido minimo es de " + rules.getMinOrderKg() + " kg. Te faltan " + remainingKg + " kg.");
        }

        List<String> invalidItems = qtyByProduct.entrySet().stream()
                .filter(entry -> entry.getValue() < rules.getMinKgPerFlavor()
                        || entry.getValue() % rules.getMinKgPerFlavor() != 0)
                .map(entry -> "Producto " + entry.getKey() + ": minimo " + rules.getMinKgPerFlavor() + " kg")
                .toList();

        if (!invalidItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cada sabor debe pedirse en lotes de " + rules.getMinKgPerFlavor() + " kg. Revisa: "
                            + String.join(", ", invalidItems));
        }
    }

    private Long resolveOrCreateClient(Long tenantId, PublicOrderRequestDTO request) {
        String phone = normalize(request.getPhone());
        if (phone != null) {
            return clientRepository.findFirstByTenantIdAndPhone(tenantId, phone)
                    .map(Client::getId)
                    .orElseGet(() -> createClientAndReturnId(tenantId, request, phone));
        }
        return createClientAndReturnId(tenantId, request, null);
    }

    private Long createClientAndReturnId(Long tenantId, PublicOrderRequestDTO request, String normalizedPhone) {
        CreateClientDTO dto = new CreateClientDTO();
        dto.setName(normalize(request.getName()));
        dto.setBusinessName(normalize(request.getBusinessName()));
        dto.setPhone(normalizedPhone);
        dto.setCity(normalize(request.getCity()));
        dto.setPreferredTransport(normalize(request.getPreferredTransport()));
        Client client = Client.builder()
                .tenantId(tenantId)
                .name(dto.getName())
                .businessName(dto.getBusinessName())
                .phone(dto.getPhone())
                .city(dto.getCity())
                .preferredTransport(dto.getPreferredTransport())
                .build();
        return clientRepository.save(client).getId();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<CreateOrderItemDTO> toOrderItems(PublicOrderRequestDTO request) {
        return request.getItems().stream().map(item -> {
            CreateOrderItemDTO dto = new CreateOrderItemDTO();
            dto.setProductId(item.getProductId());
            dto.setQuantity(item.getQuantity());
            return dto;
        }).toList();
    }
}
