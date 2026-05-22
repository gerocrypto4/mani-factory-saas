package com.manifactory.backend.tenants.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TenantResponseDTO {
    private Long id;
    private String name;
    private String slug;
    private Boolean active;
    private LocalDateTime createdAt;
}
