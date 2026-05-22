package com.manifactory.backend.clients.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ClientResponseDTO {
    private Long id;
    private Long tenantId;
    private String name;
    private String businessName;
    private String email;
    private String phone;
    private String city;
    private String preferredTransport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
