package com.manifactory.backend.clients.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientOrderHistoryResponseDTO {
    private Long clientId;
    private String clientName;
    private long totalOrders;
    private ClientOrderHistoryItemDTO latestOrder;
    private List<ClientOrderHistoryItemDTO> recentOrders;
}
