package com.manifactory.backend.tenants.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTenantDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private Boolean active = true;
}
