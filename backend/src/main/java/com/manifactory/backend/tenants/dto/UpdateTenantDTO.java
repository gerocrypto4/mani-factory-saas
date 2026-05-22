package com.manifactory.backend.tenants.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTenantDTO {
    @NotBlank
    private String name;

    private Boolean active;
}
